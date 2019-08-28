/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.tensorflowcnn;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.imageio.ImageIO;
import lt.lb.commons.ArrayOp;
import lt.lb.commons.F;
import lt.lb.commons.Lambda;
import lt.lb.commons.Log;
import lt.lb.commons.Predicates;
import lt.lb.commons.containers.tuples.Pair;
import lt.lb.commons.misc.Range;
import lt.lb.commons.threads.FastWaitingExecutor;
import lt.lb.commons.threads.sync.WaitTime;

/**
 *
 * @author laim0nas100
 */
public class FolderImageProcessing {

    public static <T> void convert(Path src, Path dest, Lambda.L2S<Path> cons) throws IOException {
        Files.createDirectories(dest);
        Files.newDirectoryStream(src).forEach(ac -> {
            F.unsafeRun(() -> {
                Path get = Paths.get(dest.toString(), ac.getFileName().toString());
//                Log.print("Got path", get);
                cons.accept(ac, get);
            });

        });
    }

    public static interface ImgFilter extends Lambda.L1R<BufferedImage, Runnable> {

    }

    public static Set<Pair<Integer>> propogate(Pair<Integer> starting, Range<Integer> rangeX, Range<Integer> rangeY, boolean withDiagnols, int current) {
        HashSet<Pair<Integer>> visited = new HashSet<>();
        propogate(starting, visited, rangeX, rangeY, withDiagnols, current);
        return visited;
    }

    public static void propogate(Pair<Integer> starting, HashSet<Pair<Integer>> visited, Range<Integer> rangeX, Range<Integer> rangeY, boolean withDiagnols, int current) {
        if (current <= 0) {
            return;
        }

        ArrayList<Pair<Integer>> list = new ArrayList<>(withDiagnols ? 8 : 4);

        if (withDiagnols) {
            list.add(new Pair<>(starting.g1 + 1, starting.g2 + 1));
            list.add(new Pair<>(starting.g1 - 1, starting.g2 - 1));
            list.add(new Pair<>(starting.g1 + 1, starting.g2 - 1));
            list.add(new Pair<>(starting.g1 - 1, starting.g2 + 1));
        }

        list.add(new Pair<>(starting.g1 + 1, starting.g2));
        list.add(new Pair<>(starting.g1, starting.g2 + 1));
        list.add(new Pair<>(starting.g1 - 1, starting.g2));
        list.add(new Pair<>(starting.g1, starting.g2 - 1));

        Predicate<Pair<Integer>> inRange = p -> {
            return rangeX.inRangeInclusive(p.g1) && rangeY.inRangeInclusive(p.g2);
        };

        Predicate<Pair<Integer>> notVisited = p -> !visited.contains(p);

        F.filterInPlace(list, inRange.and(notVisited));

        visited.addAll(list);
        F.iterate(list, (i, p) -> {
            propogate(p, visited, rangeX, rangeY, withDiagnols, current - 1);
        });

    }

    static ImgFilter noiseFilter(int range, boolean includeDiagnols, int minCount) {
        return read -> {
            int w = read.getWidth();
            int h = read.getHeight();
            ArrayList<Runnable> actions = new ArrayList<>();

            Range<Integer> rangeX = Range.of(0, w - 1);
            Range<Integer> rangeY = Range.of(0, h - 1);
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int rgb = read.getRGB(x, y);
                    Color color = new Color(rgb);
                    Collection<Pair<Integer>> propogate = propogate(new Pair<>(x, y), rangeX, rangeY, includeDiagnols, range);
                    Color[] nearby = new Color[propogate.size()];

                    F.iterate(propogate, (i, p) -> {
                        nearby[i] = new Color(read.getRGB(p.g1, p.g2));
                    });

                    int count = ArrayOp.count(p -> Objects.equals(color, p), nearby);

                    if (minCount > count) {
                        // our color is outlier
                        int xx = x;
                        int yy = y;

                        actions.add(() -> {
                            Color anyColor = nearby[0];
                            read.setRGB(xx, yy, Color.WHITE.getRGB());
                        });
                    }

                }
            }
            return () -> {
                actions.forEach(Runnable::run);
            };
        };
    }

    static ImgFilter binaryFilter() {
        return read -> {
            int w = read.getWidth();
            int h = read.getHeight();
            ArrayList<Runnable> actions = new ArrayList<>();
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int rgb = read.getRGB(x, y);
                    Color color = new Color(rgb);

                    boolean any = ArrayOp.any(i -> i < 255, color.getRed(), color.getBlue(), color.getGreen());
                    int xx = x;
                    int yy = y;
                    actions.add(() -> {

                        if (!any) {

                            read.setRGB(xx, yy, Color.WHITE.getRGB());
                            //ignore
                        } else {

                            read.setRGB(xx, yy, Color.BLACK.getRGB());
                        }
                    });

                }
            }
            return () -> {
                actions.forEach(Runnable::run);
            };
        };
    }

    public static void preparedFilter(Path src, Path dest) throws IOException {
        InputStream in = Files.newInputStream(src);
        OutputStream out = Files.newOutputStream(dest);
        BufferedImage read = ImageIO.read(in);
        binaryFilter().apply(read).run();
        noiseFilter(3, true, 25).apply(read).run();
        noiseFilter(1, true, 3).apply(read).run();
        ImageIO.write(read, "png", out);
        in.close();
        out.close();

    }

    public static String rootUrl = "E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\";

    public static void main(String... args) throws Exception {
        Log.main().stackTrace = false;

        
        //case 4
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_4\\train");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_4\\test");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_4\\validate");

        //case 10
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_10\\train");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_10\\test");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_10\\validate");
        
        //case 70
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_70\\train");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_70\\test");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_70\\validate");
        
        
        //case 111
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_111\\train");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_111\\test");
        doOther("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_111\\validate");
        
        
//        doOther();
//        F.unsafeRun(() -> {
//            Path src = Paths.get(rootUrl + "test\\img2.png");
//            Path dest = Paths.get(rootUrl + "test\\img2Out.png");
//            F.unsafeRun(() -> {
//                preparedFilter(src, dest);
//            });
//        });
//        
//        F.unsafeRun(() -> {
//            Path src = Paths.get(rootUrl + "test\\img.png");
//            Path dest = Paths.get(rootUrl + "test\\imgOut.png");
//            F.unsafeRun(() -> {
//                preparedFilter(src, dest);
//            });
//        });
//
//        Log.await(1, TimeUnit.HOURS);
//        Log.close();

    }

    public static void doOther(String path) throws IOException, InterruptedException{
        ExecutorService exe = Executors.newFixedThreadPool(4);
        Path root = Paths.get(path);
        Log.print(path);
        Files.newDirectoryStream(root).forEach(src -> {
            F.unsafeRun(() -> {
                Path dest = Paths.get(path+"CNV\\" + src.getFileName().toString());
                exe.submit(() -> {
                    convert(src, dest, (c1, c2) -> {
                        F.unsafeRun(() -> {
                            preparedFilter(c1, c2);
                        });

                    });
                    return null;
                });

            });

        });
        exe.shutdown();
        exe.awaitTermination(1, TimeUnit.DAYS);
    }
    
    public static void doOther() throws IOException, InterruptedException {
        ExecutorService exe = Executors.newFixedThreadPool(4);
        Path root = Paths.get("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_10\\train");
        Files.newDirectoryStream(root).forEach(src -> {
            F.unsafeRun(() -> {
                Path dest = Paths.get("E:\\Dev\\Java\\Workspace\\TensorFlowCNN\\img\\no_of_classes_10\\trainCnv3\\" + src.getFileName().toString());
                exe.submit(() -> {
                    convert(src, dest, (c1, c2) -> {
                        F.unsafeRun(() -> {
                            preparedFilter(c1, c2);
                        });

                    });
                    return null;
                });

            });

        });
        exe.shutdown();
        exe.awaitTermination(1, TimeUnit.DAYS);
    }

}
