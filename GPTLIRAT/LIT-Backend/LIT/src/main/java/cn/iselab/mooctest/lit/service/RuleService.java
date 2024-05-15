package cn.iselab.mooctest.lit.service;

import cn.iselab.mooctest.lit.model.StepInfo;

public interface RuleService {
    int[] executeMatchRules(StepInfo stepInfo);
    int[] imageRulesMatch(StepInfo stepInfo);
    int[] textRulesMatch(StepInfo stepInfo);
    int[] chatGPTServerMatch(StepInfo stepInfo);
    boolean executeSceneRules(StepInfo stepInfo);
    int redundantStepDetermination(String[] dirsLocation,int ind,StepInfo stepInfo);
    boolean executeSearchRules(StepInfo stepInfo);
}
