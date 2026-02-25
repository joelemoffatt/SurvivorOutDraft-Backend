package com.vivida.game.juryVote;

/**
 * DTO for JuryVote response - breaks circular references
 */
public class JuryVoteDTO {
    public Integer id;
    public Integer episodeId;
    public Integer castawayId;
    public String castawayName;
    public Integer votedForId;
    public String votedForName;

    public JuryVoteDTO(JuryVote juryVote) {
        this.id = juryVote.getId();
        
        if (juryVote.getEpisode() != null) {
            this.episodeId = juryVote.getEpisode().getId();
        }
        
        if (juryVote.getCastaway() != null) {
            this.castawayId = juryVote.getCastaway().getId();
            if (juryVote.getCastaway().getCastaway() != null) {
                this.castawayName = juryVote.getCastaway().getCastaway().getName();
            }
        }
        
        if (juryVote.getVotedFor() != null) {
            this.votedForId = juryVote.getVotedFor().getId();
            if (juryVote.getVotedFor().getCastaway() != null) {
                this.votedForName = juryVote.getVotedFor().getCastaway().getName();
            }
        }
    }
}
