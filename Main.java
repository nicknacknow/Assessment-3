// Run with `java Main.java test`
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static boolean iUsedAi() {
        // TODO: please implement
        throw new RuntimeException("Please replace this throw clause with your true/false answer to indicate whether you used AI");
    }

    public static String aiExplanation() {
        // TODO: please implement
        throw new RuntimeException("Please replace this throw class by returning a string, in which you state why or why not you used AI, what tool you used, or which other resources you relied on instead.");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main test | $file.nha");
            return;
        }

        String inputFile = args[0];
        if (!inputFile.endsWith(".nha") && !inputFile.equals("test")) {
            System.err.println("Unrecognized command or file type: " + inputFile);
            return;
        }

        if (inputFile.equals("test")) {
            test();
            return;
        }

        // looks like inputFile is indeed a file with an .nha ending
        int suffix = inputFile.lastIndexOf(".");
        String outputFile = inputFile.substring(0, suffix) + ".bin";
        try {
            Assembler asm = new Assembler(inputFile, outputFile);
            asm.assemble();
        } catch (IOException ex) {
            System.err.println("Exception parsing: " + inputFile);
            System.err.println(ex.toString());
        }
    }

    private static void test() {
        String[] testNames = new String[]{"AInst21", "CInst", "Add"};
        String[] testInput = new String[]{
                TestInput.AInst21, TestInput.CInstAsm,
                TestInput.AddAsm};
        String[] testOutput = new String[]{
                TestOutput.AInst21Bin, TestOutput.CInstBin,
                TestOutput.AddBin};

        for (int i = 0; i < testNames.length; i += 1) {
            runTest(testNames[i], testInput[i].trim(), testOutput[i].trim());
        }

        System.out.println("\n");

        try {
            boolean usedAi = iUsedAi();
            System.out.println("I used AI: " + usedAi);
        } catch (RuntimeException ex) {
            System.err.println("Main.iUsedAi() method not yet adapted");
            System.err.println(ex.getMessage());
        }

        try {
            String reasoning = aiExplanation();
            System.out.println("My reasoning: " + reasoning);
        } catch (RuntimeException ex) {
            System.err.println("Main.aiExplanation() method not yet adapted");
            System.err.println(ex.getMessage());
        }
    }

    private static void runTest(String name, String input, String expected) {
        StringWriter output = new StringWriter();
        Assembler asm = new Assembler(input, output);

        try {
            asm.assemble();
        } catch (IOException ex) {
            System.err.println("Exception parsing test input for " + name);
            return;
        } catch (Throwable t) {
            System.err.println("Test failed with exception: " + name);
            System.err.println(t.toString());
            return;
        }

        String outputStr = output.toString().trim();

        if (expected.equals(outputStr)) {
            System.out.println("Test " + name + " passed.");
        } else {
            System.out.println("Test " + name + " failed.");
            printDiff(expected, outputStr, asm.getInput());
        }
    }

    private static void printDiff(String expected, String actual, List<String> input) {
        String[] expectedLines = expected.split("\n");
        String[] actualLines = actual.split("\n");

        int inputLine = 0;
        int i = 0;
        for (; i < expectedLines.length; i += 1, inputLine += 1) {
            while (inputLine < input.size() && input.get(inputLine).isEmpty()) {
                inputLine += 1;
            }

            String instruction = inputLine < input.size() ? input.get(inputLine) : "";

            if (actualLines.length <= i) {
                System.err.printf("line %3d: %s\t", i + 1, instruction);
                System.err.println(expectedLines[i] + " != missing");
                continue;
            }

            if (!expectedLines[i].equals(actualLines[i])) {
                System.err.printf("line %3d: %s\t", i + 1, instruction);
                System.err.println(expectedLines[i] + " != " + actualLines[i]);
            }
        }

        for (; i < actualLines.length; i += 1) {
            while (inputLine < input.size() && input.get(inputLine).isEmpty()) {
                inputLine += 1;
            }

            String instruction = inputLine < input.size() ? input.get(inputLine) : "";

            System.err.printf("line %3d: %s\t", i + 1, instruction);
            System.err.println(" != " + actualLines[i]);
        }
    }
}

class TestInput {
    public final static String AInst21 = "ldr A, $21";

    public final static String CInstAsm = """
            ldr D, (A)
            sub D, D, (A)
            jgt D
            ldr D, (A)
            jmp
            str (A), D
            """;

    public final static String AddAsm = """
            ldr A, $2
            ldr D, A
            ldr A, $3
            add D, D, A
            ldr A, $0
            str (A), D
            """;
}

class TestOutput {
    public static final String AInst21Bin = "0000000000010101";

    public final static String CInstBin = """
            1111110000010000
            1111010011010000
            1110001100000001
            1111110000010000
            1110101010000111
            1110001100001000
            """;

    public static final String AddBin = """
            0000000000000010
            1110110000010000
            0000000000000011
            1110000010010000
            0000000000000000
            1110001100001000""";
}

class Assembler {
    private final List<String> input;
    private final Writer output;

    public Assembler(String inputFile, String outputFile) throws IOException {
        input = Files.readAllLines(Paths.get(inputFile));
        output = new PrintWriter(new FileWriter(outputFile));
    }

    public Assembler(String input, StringWriter output) {
        this.input = Arrays.asList(input.split(System.lineSeparator()));
        this.output = output;
    }

    private void output(String bin) throws IOException {
        output.write(bin);
        output.write('\n');
    }

    public void assemble() throws IOException {
        // TODO: to be implemented
        List<String> input = getInput();
        for (String string : input) {
            System.out.println("heeeeeeeeeeeeeeeeeeellooooo");

            List<String> ops = Arrays.asList(string.split(" "));

            System.out.println(ops.getFirst());

            String opcode = ops.remove(0); // why this and removeFirst causing issues ? 
            System.out.println("heeeeeeeeeeeeeeeeeeellooooo");
            System.out.println("heeeeeeeeeeeeeeeeeeellooooo");
            
            System.out.println(ops.getFirst());
            
            switch (opcode) {
                case "ldr":
                    
                    break;
                case "str":

                    break;
                case "add":

                    break;
                default:
                    break;
            }
        }
    }

    public List<String> getInput() {
        return input;
    }
}
