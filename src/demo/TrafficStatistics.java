package demo;

import demo.domain.MaxAndMinMap;
import demo.domain.UserVisitLog;
import demo.domain.VisitInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TrafficStatistics {

    private static final int SMALL_FILE_NUM = 50;

    /**
     * 分割大文件
     *
     * @param file 文件
     */
    public void splitFile(File file) throws Exception {
        try (FileReader fileReader = new FileReader(file);
             BufferedReader br = new BufferedReader(fileReader)) {
            int line = 0;
            while (true) {
                String s = br.readLine();
                line++;
                if (Objects.isNull(s)) {
                    break;
                }
                if (line == 1) {
                    continue;
                }
                UserVisitLog userVisitLog = parseLine(s);
                int index = userVisitLog.getUrl().hashCode() % SMALL_FILE_NUM;
                BufferedWriter[] bufferedWriters = new BufferedWriter[SMALL_FILE_NUM];
                for (int i = 0; i < SMALL_FILE_NUM; i++) {
                    bufferedWriters[i] = new BufferedWriter(new FileWriter(file.getName() + "_sp_" + i));
                }
                bufferedWriters[index].write(s);
                bufferedWriters[index].newLine();
                closeAll(bufferedWriters);
            }
        }
    }

    /**
     * 任务拆分处理，开启多线程或者使用多个服务
     *
     * @param filePathPrefix
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<MaxAndMinMap> batchRead(String filePathPrefix) throws InterruptedException, ExecutionException {
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4, 60, TimeUnit.SECONDS, blockingQueue);
        CountDownLatch countDownLatch = new CountDownLatch(SMALL_FILE_NUM);
        List<Future<MaxAndMinMap>> list = new ArrayList<>();
        for (int i = 0; i < SMALL_FILE_NUM; i++) {
            Future<MaxAndMinMap> future = threadPoolExecutor.submit(new ReadTask(filePathPrefix + i, countDownLatch));
            list.add(future);
        }
        countDownLatch.await();
        threadPoolExecutor.shutdown();
        List<MaxAndMinMap> results = new ArrayList<>();
        for (Future<MaxAndMinMap> future : list) {
            if (future.isDone()) {
                results.add(future.get());
            }
        }
        return results;
    }

    /**
     * 汇总
     *
     * @param results
     */
    public void handleResult(List<MaxAndMinMap> results) {
        List<VisitInfo> max = new ArrayList<>();
        List<VisitInfo> min = new ArrayList<>();
        for (MaxAndMinMap e : results) {
            if (Objects.nonNull(e))
                max.addAll(e.getMaxVisitInfos());
            min.addAll(e.getMinVisitInfos());
        }

        String maxVisitUrls = max.parallelStream()
                .sorted(Comparator.comparing(VisitInfo::getCount).reversed())
                .limit(20)
                .map(VisitInfo::getUrl)
                .collect(Collectors.joining(","));
        System.out.println(maxVisitUrls);
        System.out.println("Top20的前20用户如下：");
        max.parallelStream()
                .sorted(Comparator.comparing(VisitInfo::getCount).reversed())
                .limit(20)
                .forEach(e -> {
                    System.out.println("url：" + e.getUrl() + ", 用户：" + e.getIds());
                });
        String minVisitUrls = min.parallelStream()
                .sorted(Comparator.comparing(VisitInfo::getCount).reversed())
                .limit(20)
                .map(VisitInfo::getUrl)
                .collect(Collectors.joining(","));
        System.out.println(minVisitUrls);
    }

    private void closeAll(BufferedWriter[] bufferedWriters) throws Exception {
        for (BufferedWriter writer : bufferedWriters) {
            writer.close();
        }
    }

    public static UserVisitLog parseLine(String s) {
        String[] split = s.split(",");
        UserVisitLog userVisitLog = new UserVisitLog();
        userVisitLog.setId(split[0]);
        userVisitLog.setUrl(split[1]);
        userVisitLog.setDate(split[2]);
        return userVisitLog;
    }
}
