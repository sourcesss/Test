package demo.domain;

public class VisitInfo {
    private String url;
    private Long count;
    private String ids;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "VisitInfo{" +
                "url='" + url + '\'' +
                ", count=" + count +
                ", ids='" + ids + '\'' +
                '}';
    }
}
