# CI/CD to Cloud Run (GitHub Actions)

This project now includes:

- GitHub Actions workflow: `.github/workflows/deploy-cloud-run.yml`
- Container build file: `spring-boot/Dockerfile`

The workflow runs tests, builds a Docker image, pushes it to Artifact Registry, and deploys to Cloud Run on every push to `main` that touches the backend.

## 0. Set shell secrets/variables first

Before running any setup commands, export these in your shell:

```bash
export GCP_PROJECT_ID="project-3a4e8cbe-52bf-4e46-9e9"
export GCP_REGION="us-central1"
export ARTIFACT_REGISTRY_REPO="survivor-images"
export CLOUD_RUN_SERVICE="survivor-outdraft-backend"
export GCP_SA_NAME="github-actions-deployer"
export GCP_SA_DISPLAY_NAME="GitHub Actions Cloud Run Deployer"
export GITHUB_REPO="joelemoffatt/SurvivorOutDraft-Backend"
export FRONTEND_GITHUB_REPO="joelemoffatt/survivor-outdraft-frontend"
export WIF_POOL_ID="github-pool"
export WIF_PROVIDER_ID="github-provider"

# App/runtime secrets (also used as GitHub Actions secrets later)
export SPRING_DATASOURCE_URL="jdbc:postgresql://..."
export SPRING_DATASOURCE_USERNAME="..."
export SPRING_DATASOURCE_PASSWORD="..."
export JWT_SECRET="..."
export VIVIDA_CORS_ALLOWED_ORIGIN_PATTERNS="https://project-3a4e8cbe-52bf-4e46-9e9.web.app"
```

Optional check to confirm everything is set:

```bash
for v in GCP_PROJECT_ID GCP_REGION ARTIFACT_REGISTRY_REPO CLOUD_RUN_SERVICE GCP_SA_NAME GITHUB_REPO FRONTEND_GITHUB_REPO  WIF_POOL_ID WIF_PROVIDER_ID SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD JWT_SECRET VIVIDA_CORS_ALLOWED_ORIGIN_PATTERNS; do
  [[ -z "${!v}" ]] && echo "Missing: $v"
done
```

## 1. One-time Google Cloud setup

Run these commands once:

```bash
gcloud config set project "$GCP_PROJECT_ID"
gcloud services enable run.googleapis.com artifactregistry.googleapis.com iam.googleapis.com cloudbuild.googleapis.com cloudresourcemanager.googleapis.com

export GCP_SA_EMAIL="$GCP_SA_NAME@$GCP_PROJECT_ID.iam.gserviceaccount.com"

gcloud artifacts repositories create "$ARTIFACT_REGISTRY_REPO" \
  --repository-format=docker \
  --location="$GCP_REGION"

gcloud iam service-accounts create "$GCP_SA_NAME" \
  --display-name="$GCP_SA_DISPLAY_NAME"

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:$GCP_SA_EMAIL" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:$GCP_SA_EMAIL" \
  --role="roles/artifactregistry.writer"

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:$GCP_SA_EMAIL" \
  --role="roles/iam.serviceAccountUser"

gcloud projects add-iam-policy-binding "$GCP_PROJECT_ID" \
  --member="serviceAccount:$GCP_SA_EMAIL" \
  --role="roles/logging.viewer"
```

## 2. Configure Workload Identity Federation (keyless auth)

```bash
export GCP_PROJECT_NUMBER="$(gcloud projects describe "$GCP_PROJECT_ID" --format='value(projectNumber)')"

gcloud iam workload-identity-pools create "$WIF_POOL_ID" \
  --project="$GCP_PROJECT_ID" \
  --location="global" \
  --display-name="GitHub Pool"

gcloud iam workload-identity-pools providers create-oidc "$WIF_PROVIDER_ID" \
  --project="$GCP_PROJECT_ID" \
  --location="global" \
  --workload-identity-pool="$WIF_POOL_ID" \
  --display-name="GitHub Provider" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository=='$GITHUB_REPO' || assertion.repository=='$FRONTEND_GITHUB_REPO'"

gcloud iam service-accounts add-iam-policy-binding "$GCP_SA_EMAIL" \
  --project="$GCP_PROJECT_ID" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$GCP_PROJECT_NUMBER/locations/global/workloadIdentityPools/$WIF_POOL_ID/attribute.repository/$GITHUB_REPO"

gcloud iam service-accounts add-iam-policy-binding "$GCP_SA_EMAIL" \
  --project="$GCP_PROJECT_ID" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/$GCP_PROJECT_NUMBER/locations/global/workloadIdentityPools/$WIF_POOL_ID/attribute.repository/$FRONTEND_GITHUB_REPO"

export GCP_WORKLOAD_IDENTITY_PROVIDER="projects/$GCP_PROJECT_NUMBER/locations/global/workloadIdentityPools/$WIF_POOL_ID/providers/$WIF_PROVIDER_ID"
```

If the provider already exists, update it with the same multi-repo condition:

```bash
gcloud iam workload-identity-pools providers update-oidc "$WIF_PROVIDER_ID" \
  --project="$GCP_PROJECT_ID" \
  --location="global" \
  --workload-identity-pool="$WIF_POOL_ID" \
  --attribute-condition="assertion.repository=='$GITHUB_REPO' || assertion.repository=='$FRONTEND_GITHUB_REPO'"
```

You no longer need service account key creation (`gcloud iam service-accounts keys create ...`).

## 3. Add GitHub repository secrets

In GitHub: Settings -> Secrets and variables -> Actions -> New repository secret.

Set these GitHub Actions secrets (name must match exactly):

| GitHub Secret Name | Value Source | Command if missing |
|---|---|---|
| GCP_PROJECT_ID | Your Google Cloud project ID | `gcloud config get-value project` |
| GCP_REGION | Cloud Run / Artifact Registry region | `echo "$GCP_REGION"` (or set one, for example `export GCP_REGION="us-central1"`) |
| ARTIFACT_REGISTRY_REPO | Docker repository name in Artifact Registry | `echo "$ARTIFACT_REGISTRY_REPO"` or `gcloud artifacts repositories list --location "$GCP_REGION" --format='value(name)'` |
| CLOUD_RUN_SERVICE | Cloud Run service name | `echo "$CLOUD_RUN_SERVICE"` or `gcloud run services list --region "$GCP_REGION" --format='value(metadata.name)'` |
| GCP_WORKLOAD_IDENTITY_PROVIDER | Full WIF provider resource path | `echo "$GCP_WORKLOAD_IDENTITY_PROVIDER"` or `echo "projects/$GCP_PROJECT_NUMBER/locations/global/workloadIdentityPools/$WIF_POOL_ID/providers/$WIF_PROVIDER_ID"` |
| GCP_SERVICE_ACCOUNT | Deployer service account email | `echo "$GCP_SA_EMAIL"` or `gcloud iam service-accounts list --project "$GCP_PROJECT_ID" --format='value(email)' | grep "$GCP_SA_NAME"` |
| SPRING_DATASOURCE_URL | JDBC URL for Neon/Postgres | `echo "$SPRING_DATASOURCE_URL"` |
| SPRING_DATASOURCE_USERNAME | Database username | `echo "$SPRING_DATASOURCE_USERNAME"` |
| SPRING_DATASOURCE_PASSWORD | Database password | `echo "$SPRING_DATASOURCE_PASSWORD"` |
| JWT_SECRET | Production JWT secret | `echo "$JWT_SECRET"` or generate one with `openssl rand -hex 64` |
| VIVIDA_CORS_ALLOWED_ORIGIN_PATTERNS | Allowed frontend origins | `echo "$VIVIDA_CORS_ALLOWED_ORIGIN_PATTERNS"` |

Optional: quick local check before adding secrets in GitHub.

```bash
for v in GCP_PROJECT_ID GCP_REGION ARTIFACT_REGISTRY_REPO CLOUD_RUN_SERVICE GCP_WORKLOAD_IDENTITY_PROVIDER GCP_SA_EMAIL SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME SPRING_DATASOURCE_PASSWORD JWT_SECRET VIVIDA_CORS_ALLOWED_ORIGIN_PATTERNS; do
  [[ -z "${!v}" ]] && echo "Missing locally: $v" || echo "Ready: $v"
done
```

## 4. Push to deploy

Push changes to `main`. GitHub Actions will:

1. run backend tests
2. build and push container image
3. deploy to Cloud Run with env vars from GitHub secrets

You can also run it manually from the Actions tab with `workflow_dispatch`.

## 5. Verify deployment

```bash
gcloud run services describe "$CLOUD_RUN_SERVICE" --region "$GCP_REGION" --format='value(status.url)'
```

Open that URL and verify the API is reachable.
