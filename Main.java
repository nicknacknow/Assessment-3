// Run with `java Main.java test`
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry; // use this just to make mapping encoding stuff less ugly

public class Main {
    public static boolean iUsedAi() {
        // TODO: please implement
        return false;
    }

    public static String aiExplanation() {
        // TODO: please implement
        return "i am better than that";
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
        this.input = Arrays.asList(input.split("(\n)|(\r\n)|(\r)"));
        this.output = output;
    }

    private void output(String bin) throws IOException {
        output.write(bin);
        output.write('\n');
    }

    private String IntTo15BitBinary(int n) {
        int num = n;

        String binary = "";
        for (int i = 14; i >= 0; i--)
        {
            int int_max = (int)Math.pow(2, i);

            if (num >= int_max) {                
                binary += "1";
                num -= int_max;
            }
            else binary += "0";
        }

        return binary;
    }

    // is there a better way to do this? e.g. each c bit has specific meaning
    Map<String, String> compBitDict = Map.ofEntries(
        entry("0", "101010"),
        entry("1", "111111"),
        entry("-1", "111010"),
        entry("D", "001100"),
        entry("A", "110000"),
        entry("!D", "001101"),
        entry("!A", "110001"),
        entry("D+1", "001111"),
        entry("A+1", "110111"),
        entry("D-1", "001110"),
        entry("A-1", "110010"),
        entry("D+A", "000010"),
        entry("D-A", "010011"),
        entry("A-D", "000111"),
        entry("D&A", "000000"),
        entry("D|A", "010101")
    );

    private String FormCompBitString(String comp) {
        boolean contains_m = comp.contains("M");
        if (contains_m) { // little hack so i dont have repeat entries in map
            comp = comp.replaceAll("M", "A");
        }
        // ternary operator, if it contains m, we want a=1 otherwise a=0
        return ((contains_m ? "1" : "0") + compBitDict.get(comp));
    }

    Map<String, String> destBitDict = Map.ofEntries(
        entry("null", "000"),  // The value is not stored
        entry("M", "001"),     // RAM[A]
        entry("D", "010"),     // D register
        entry("MD", "011"),    // RAM[A] and D register
        entry("A", "100"),     // A register
        entry("AM", "101"),    // A register and RAM[A]
        entry("AD", "110"),    // A register and D register
        entry("AMD", "111")    // A register, RAM[A], and D register
    );

    private String FormDestBitString(String dest) {
        return destBitDict.get(dest);
    }

    // we assume sComp contains 'a' bit. (a=0 or a=1 for comp)
    private String FormCInstruction(String sComp, String sDest, String sJump) {
        return "111" + sComp + sDest + sJump;
    }

    public void assemble() throws IOException {
        
        // TODO: to be implemented
        List<String> input = getInput();
        for (String string : input) {
            System.out.println("output: " + string);
            
            String opcode = string.substring(0, 3);
            
            String operands_string = string.substring(3, string.length());
            operands_string = operands_string.replaceAll("\\s+", ""); // remove whitespace
            
            String[] operands = operands_string.split(",");
            
            // need to check for jmps such as jgt, jlt, jeq, ... just check first char=j then do switch

            switch (opcode) {
                case "ldr": {
                    System.out.println("ldr: ");
                    for (String op : operands) {
                        System.out.println("    " + op);
                    }

                    if (operands[0].equals("A")) {
                        String op2 = operands[1];
                        int val = Integer.parseInt(op2.substring(1));

                        // call IntTo15BitBinary (& find a better name) and write to output method
                        output("0" + IntTo15BitBinary(val));
                    }
                    else {
                        String target = operands[0];
                        String src = operands[1];
                        
                        src = src.replaceAll("\\(A\\)", "M");
                        // code above does code below --- will A be the only value that appears in parentheses? 

                        /*if (!src.equals(src.replaceAll("[()]", ""))) { // this means parentheses are present, indicating we need to read the memory of src
                            if (src.equals("(A)")) src = "M";
                            else {
                                System.out.println("check this out!!!!! " + src);
                            }
                        }*/
                        
                        String comp = FormCompBitString(src);
                        String dest = FormDestBitString(target);

                        // ensure this is correct
                        output(FormCInstruction(comp, dest, "000"));
                        //System.out.println("target: " + target + "(" + dest + ") src: " + src + "(" + comp + ")");
                    }

                    break;
                }
                case "str": {

                    break;
                }
                case "add": {

                    break;
                }
                case "sub": {

                    break;
                }
                default: break;
            }
        }
    }

    public List<String> getInput() {
        return input;
    }
}


            // http://127.0.0.1:54014/?code=bb18cef0a7a0dc6013dc&state=76be5f118c62433e892ec63b8bb6840e
            // ^^