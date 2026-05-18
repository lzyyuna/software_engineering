package com.group4.tarecruitment.service;

import com.group4.tarecruitment.model.Applicant;
import com.group4.tarecruitment.model.Job;
import com.group4.tarecruitment.model.SkillMatchResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates deterministic skill matching between TA applicant profiles and job requirements.
 */
public class SkillMatchService {

    private static final Map<String, List<String>> SKILL_KEYWORDS = new LinkedHashMap<>();

    static {
        SKILL_KEYWORDS.put("Java", List.of("java"));
        SKILL_KEYWORDS.put("Python", List.of("python"));
        SKILL_KEYWORDS.put("English", List.of("english", "communication", "spoken english", "written english"));
        SKILL_KEYWORDS.put("Teaching", List.of("teaching", "tutor", "mentoring", "instruction"));
        SKILL_KEYWORDS.put("Office", List.of("office", "excel", "word", "powerpoint"));
    }

    /**
     * Compares an applicant's skill tags with a job's skill requirements.
     *
     * @param applicant applicant profile to evaluate
     * @param job job requirement source
     * @return match result including score, matched skills, missing skills, and recommendation
     */
    public SkillMatchResult match(Applicant applicant, Job job) {
        Set<String> applicantSkills = normalizeApplicantSkills(applicant == null ? null : applicant.getSkillTags());
        Set<String> requiredSkills = normalizeJobSkills(job == null ? null : job.getSkillRequirements());

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String required : requiredSkills) {
            if (applicantSkills.contains(required)) {
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        double score = requiredSkills.isEmpty() ? 0.0 : matched.size() * 100.0 / requiredSkills.size();

        SkillMatchResult result = new SkillMatchResult();
        result.setJobId(job != null ? job.getJobId() : "");
        result.setApplicantTaId(applicant != null ? applicant.getTaId() : "");
        result.setMatchScore(score);
        result.setMatchedSkills(matched);
        result.setMissingSkills(missing);
        result.setRecommendationLevel(getRecommendationLevel(score));
        result.setAiExplanation("");
        return result;
    }

    private Set<String> normalizeApplicantSkills(String skillTags) {
        Set<String> result = new LinkedHashSet<>();
        if (skillTags == null || skillTags.isBlank()) {
            return result;
        }

        String[] parts = skillTags.split(",");
        for (String part : parts) {
            String raw = part.trim().toLowerCase();
            for (Map.Entry<String, List<String>> entry : SKILL_KEYWORDS.entrySet()) {
                String standardSkill = entry.getKey();
                for (String keyword : entry.getValue()) {
                    if (raw.contains(keyword.toLowerCase())) {
                        result.add(standardSkill);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private Set<String> normalizeJobSkills(String requirementText) {
        Set<String> result = new LinkedHashSet<>();
        if (requirementText == null || requirementText.isBlank()) {
            return result;
        }

        String text = requirementText.toLowerCase();
        for (Map.Entry<String, List<String>> entry : SKILL_KEYWORDS.entrySet()) {
            String standardSkill = entry.getKey();
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword.toLowerCase())) {
                    result.add(standardSkill);
                    break;
                }
            }
        }
        return result;
    }

    private String getRecommendationLevel(double score) {
        if (score >= 80) return "Strong Match";
        if (score >= 50) return "Moderate Match";
        if (score > 0) return "Weak Match";
        return "No Match";
    }
}
