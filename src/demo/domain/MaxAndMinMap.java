package demo.domain;

import java.util.List;

public class MaxAndMinMap {
    private List<VisitInfo> maxVisitInfos;
    private List<VisitInfo> minVisitInfos;

    public MaxAndMinMap(List<VisitInfo> maxVisitInfos, List<VisitInfo> minVisitInfos) {
        this.maxVisitInfos = maxVisitInfos;
        this.minVisitInfos = minVisitInfos;
    }

    public List<VisitInfo> getMaxVisitInfos() {
        return maxVisitInfos;
    }

    public void setMaxVisitInfos(List<VisitInfo> maxVisitInfos) {
        this.maxVisitInfos = maxVisitInfos;
    }

    public List<VisitInfo> getMinVisitInfos() {
        return minVisitInfos;
    }

    public void setMinVisitInfos(List<VisitInfo> minVisitInfos) {
        this.minVisitInfos = minVisitInfos;
    }
}
