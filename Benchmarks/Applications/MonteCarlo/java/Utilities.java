
import EDU.oswego.cs.dl.util.concurrent.*;

public final class Utilities {
    
    public Utilities() {
        super();
    }
    public static boolean DEBUG = false;
    private static String className = "Utilities";
    
    public static String which(String executable, String pathEnv) {
        String executablePath;
        String[] paths;
        paths = splitString(System.getProperty("path.separator"), pathEnv);
        for (int i = 0; i < paths.length; i++) {
            if (paths[i].length() > 0) {
                java.io.File pathFile = new java.io.File(paths[i]);
                if (pathFile.isDirectory()) {
                    String[] filesInDirectory;
                    filesInDirectory = pathFile.list();
                    for (int j = 0; j < filesInDirectory.length; j++) {
                        if (DEBUG) {
                            System.out.println("DBG: Matching " + filesInDirectory[j]);
                        }
                        if (filesInDirectory[j].equals(executable)) {
                            executablePath = paths[i] + System.getProperty("file.separator") + executable;
                            return executablePath;
                        }
                    }
                } else {
                    if (DEBUG) {
                        System.out.println("DBG: path " + paths[i] + " is not a directory!");
                    }
                }
            }
        }
        executablePath = executable + " not found.";
        return executablePath;
    }
    
    public static String joinString(String joinChar, String[] stringArray) {
        return joinString(joinChar, stringArray, 0);
    }
    
    public static String joinString(String joinChar, String[] stringArray, int index) {
        String methodName = "join";
        StringBuffer tmpString;
        int nStrings = java.lang.reflect.Array.getLength(stringArray);
        if (nStrings <= index) {
            tmpString = new StringBuffer();
        } else {
            tmpString = new StringBuffer(stringArray[index]);
            for (int i = (index + 1); i < nStrings; i++) {
                tmpString.append(joinChar).append(stringArray[i]);
            }
        }
        return tmpString.toString();
    }
    
    public static String[] splitString(String splitChar, String arg) {
        String methodName = "split";
        String[] myArgs;
        int nArgs = 0;
        int foundIndex = 0;
        int fromIndex = 0;
        while ((foundIndex = arg.indexOf(splitChar, fromIndex)) > -1) {
            nArgs++;
            fromIndex = foundIndex + 1;
        }
        if (DEBUG) {
            System.out.println("DBG " + className + "." + methodName + ": " + nArgs);
        }
        myArgs = new String[nArgs + 1];
        nArgs = 0;
        fromIndex = 0;
        while ((foundIndex = arg.indexOf(splitChar, fromIndex)) > -1) {
            if (DEBUG) {
                System.out.println("DBG " + className + "." + methodName + ": " + fromIndex + " " + foundIndex);
            }
            myArgs[nArgs] = arg.substring(fromIndex, foundIndex);
            nArgs++;
            fromIndex = foundIndex + 1;
        }
        myArgs[nArgs] = arg.substring(fromIndex);
        return myArgs;
    }
}
