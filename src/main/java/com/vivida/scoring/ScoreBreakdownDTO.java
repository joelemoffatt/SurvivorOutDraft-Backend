package com.vivida.scoring;

import java.util.List;

public class ScoreBreakdownDTO {

    public Integer teamId;
    public String teamName;
    public Integer totalPoints;
    public List<CastawayBreakdown> castaways;

    public static class CastawayBreakdown {
        public Integer teamCastawayId;
        public Integer castawayPerformanceId;
        public String castawayName;
        public Integer totalPoints;
        public List<ScoreEventDTO> scoreEvents;
    }

    public static class ScoreEventDTO {
        public Integer id;
        public Integer episodeNumber;
        public String eventLabel;
        public Integer totalPoints;
    }
}
