package com.group4.tarecruitment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the calculated skill match between a TA applicant and a job.
 */
public class SkillMatchResult {
    private String jobId;
    private String applicantTaId;
    private double matchScore;
    private List<String> matchedSkills = new ArrayList<>();
    private List<String> missingSkills = new ArrayList<>();
    private String recommendationLevel;
    private String aiExplanation;

    /**
     * Creates an empty skill match result.
     */
    public SkillMatchResult() {}

    /**
     * Creates a complete skill match result.
     *
     * @param jobId target job identifier
     * @param applicantTaId applicant TA identifier
     * @param matchScore percentage match score
     * @param matchedSkills required skills found in the applicant profile
     * @param missingSkills required skills not found in the applicant profile
     * @param recommendationLevel recommendation label based on the score
     * @param aiExplanation optional AI-generated explanation
     */
    public SkillMatchResult(String jobId, String applicantTaId, double matchScore,
                            List<String> matchedSkills, List<String> missingSkills,
                            String recommendationLevel, String aiExplanation) {
        this.jobId = jobId;
        this.applicantTaId = applicantTaId;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.recommendationLevel = recommendationLevel;
        this.aiExplanation = aiExplanation;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getApplicantTaId() {
        return applicantTaId;
    }

    public void setApplicantTaId(String applicantTaId) {
        this.applicantTaId = applicantTaId;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(double matchScore) {
        this.matchScore = matchScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getRecommendationLevel() {
        return recommendationLevel;
    }

    public void setRecommendationLevel(String recommendationLevel) {
        this.recommendationLevel = recommendationLevel;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
}
