package jungood;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author LexFH
 */
public final class JUngood {

    private static Boolean isVerbose;
    private static Boolean isKeepPD;
    private static String[] langs;
    private static final String[] versions = new String[]{"(REV"};
    private static final String[] noGoods = new String[]{"([o)", "[hI", "(Beta, (Alpha", "[f", "[p", "[c"};
    private static final String[] goods = new String[]{"[!]", "[C]"};

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        if (args.length == 0) {
            System.out.println("Usage : java -jar jUngood.jar \"path/to/roms/folder/\" options");
            System.out.println("The roms folder must contain individual roms, unziped or ziped one by one.");
            System.out.println("Options :");
            System.out.println(" -delete : applies the selection and deletes unwanted roms from your hard drive. If omitted, nothing is really done, you can safely preview selection in the output.");
            System.out.println(" -english : selects english language versions, if omitted it will be french language.");
            System.out.println(" -keeppd : keeps Public Domain roms. They are deleted by default.");
            System.out.println(" -verbose : for debug purpose.");
            //return;
        }
        isVerbose = Arrays.stream(args).anyMatch("-verbose"::equals);
        isKeepPD = !Arrays.stream(args).anyMatch("-keepPD"::equals);
        if (Arrays.stream(args).anyMatch("-english"::equals)) {
            langs = new String[]{"(U)", "[T+Eng", "(M#)", "(E)", "(UE)", "(UEB)", "(JU)", "(JUE)", "(W)", "(JU)", "(E)"};
        } else {
            langs = new String[]{"(F)", "[T+Fre", "(CF)", "(M#)", "(E)", "(UE)", "(UEB)", "(JE)", "(JUE)", "(W)", "(U)", "(JU)", "[T+Eng"};
        }
        final Boolean isDelete = true;//Arrays.stream(args).anyMatch("-delete"::equals);
        final String inputPath = "/media/lex/9bc03bd8-d628-44fb-b687-f4cf110a2beb/home/lex/emul/"; //args[0];

        final List<File> retained = new ArrayList();
        final File[] files = new File(inputPath).listFiles();
        System.out.println("Filtering " + files.length + " files");
        Arrays.sort(files);
        for (final File file : files) {
            if (file.isFile() && isInteresting(file)) {
                retained.add(file);
            } else if (isDelete) {
                file.delete();
            }
        }

        System.out.println("Grouping " + retained.size() + " files");
        final List<List<File>> groupedResults = new ArrayList<>();
        String currentGroup = getGroupName(retained.get(0).getName());
        if (isVerbose) {
            System.out.println("  +Creating group " + currentGroup);
        }
        groupedResults.add(new ArrayList<>());
        for (final File file : retained) {
            final String fileGroup = getGroupName(file.getName());
            if (!fileGroup.equals(currentGroup)) {
                currentGroup = fileGroup;
                if (isVerbose) {
                    System.out.println("  +Creating group " + currentGroup);
                }
                groupedResults.add(new ArrayList<>());
            }
            if (isVerbose) {
                System.out.println("    Adding " + file.getName());
            }
            groupedResults.get(groupedResults.size() - 1).add(file);
        }

        System.out.println("Selecting best version in " + groupedResults.size() + " groups");
        for (final List<File> group : groupedResults) {
            final LanguageComparator languageComparator = new LanguageComparator();
            Collections.sort(group, languageComparator);
            System.out.println("  + Retaining " + group.get(0).getName());
            for (int i = 1; i < group.size(); i++) {
                if (isVerbose) {
                    System.out.println("   - Deleting " + group.get(i).getName());
                }
                if (isDelete) {
                    group.get(i).delete();
                }
            }
        }
    }

    private static Boolean isInteresting(final File file) {
        final String tags = getTags(file.getName());
        if (file.getName().contains(" by ")
                || file.getName().contains("-in-1")
                || tags.contains("BIOS")
                || tags.contains("Prototype")
                || tags.contains("Hack")
                || tags.contains("Debug")
                || tags.contains("Demo")
                || tags.contains("[b")
                || tags.contains("[h")
                || tags.contains("[t")
                || (isKeepPD && tags.contains("(PD)"))) {
            if (isVerbose) {
                System.out.println("  - rejecting " + file.getName());
            }
            return Boolean.FALSE;
        }
        if (isVerbose) {
            System.out.println("  + retaining " + file.getName());
        }
        return Boolean.TRUE;
    }

    private static String getGroupName(final String string) {
        final int par = string.indexOf("(");
        final int brack = string.indexOf("[");
        if (par > 0 && brack > 0) {
            return string.substring(0, Math.min(par, brack) - 1);
        }
        if (par > 0) {
            return string.substring(0, par - 1);
        }
        try {
            return string.substring(0, brack - 1);
        } catch (final StringIndexOutOfBoundsException ex) {
            return string;
        }
    }

    private static String getTags(final String string) {
        final int par = string.indexOf("(");
        final int brack = string.indexOf("[");
        if (par > 0 && brack > 0) {
            return string.substring(Math.min(par, brack) - 1);
        }
        if (par > 0) {
            return string.substring(par - 1);
        }
        try {
            return string.substring(brack - 1);
        } catch (final IndexOutOfBoundsException ex) {
            return "";
        }
    }

    private static final class LanguageComparator implements Comparator<File> {

        @Override
        public int compare(final File a, final File b) {
            final String aName = a.getName();
            final String bName = b.getName();

            if (aName.startsWith("Alisha's Adventure") || bName.startsWith("Alisha's Adventure")) {
                System.out.println();
            }

            if (isVerbose) {
                System.out.println("  comparing " + aName);
                System.out.println("         to " + bName);
            }

            //compares language
            for (final String crit : langs) {
                if (aName.contains(crit) && !bName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + aName);
                    }
                    return -1;
                }
                if (bName.contains(crit) && !aName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + bName);
                    }
                    return 1;
                }
                if (aName.contains("[T+Fre") && aName.contains("[T+Fre")) {
                    if (isVerbose) {
                        System.out.println("      prefering " + Math.negateExact(aName.compareTo(bName)));
                    }
                    return Math.negateExact(aName.compareTo(bName));
                }
                if (aName.contains("[T+Eng") && aName.contains("[T+Eng")) {
                    if (isVerbose) {
                        System.out.println("      prefering " + Math.negateExact(aName.compareTo(bName)));
                    }
                    return Math.negateExact(aName.compareTo(bName));
                }
                //Tries to get a non japanese version
                if (aName.contains("[J]") && bName.contains("[J]")) {
                    if (aName.contains("[T+") && !bName.contains("[T+")) {
                        if (isVerbose) {
                            System.out.println("      prefering " + aName);
                        }
                        return -1;
                    }
                    if (bName.contains("[T+") && !aName.contains("[T+")) {
                        if (isVerbose) {
                            System.out.println("      prefering " + aName);
                        }
                        return 1;
                    }
                }
            }

            //compares version
            for (final String crit : versions) {
                if (aName.contains(crit) && bName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + Math.negateExact(aName.compareTo(bName)));
                    }
                    return Math.negateExact(aName.compareTo(bName));
                }
            }

            //downgrades unpleasant factors
            for (final String crit : noGoods) {
                if (aName.contains(crit) && !bName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + bName);
                    }
                    return 1;
                }
                if (bName.contains(crit) && !aName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + aName);
                    }
                    return -1;
                }
            }

            //upgrades good factors
            for (final String crit : goods) {
                if (aName.contains(crit) && !bName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + aName);
                    }
                    return -1;
                }
                if (bName.contains(crit) && !aName.contains(crit)) {
                    if (isVerbose) {
                        System.out.println("      prefering " + bName);
                    }
                    return 1;
                }
            }
            if (isVerbose) {
                System.out.println("      equality");
            }
            return 0;
        }
    }
}
