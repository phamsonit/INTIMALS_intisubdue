package convert;

public class Main {
    public static void main(String args[]) {
        try {
            String inPath = "";
            String outPath = "";
            boolean ast = false; //if ast = true, convert ASTs to graphs

            if(args.length==0){
                System.out.println("USAGE:");
                System.out.println("Convert groums to graphs:");
                System.out.println("java -jar subdueConverter.jar INPUT_DIRECTORY OUTPUT_FILE_NAME.json");
                System.out.println("Convert ASTs to graphs:");
                System.out.println("java -jar subdueConverter.jar INPUT_DIRECTORY OUTPUT_FILE_NAME.json --ast");
                System.exit(-1);
            }else{
                if(args.length==2){
                    inPath = args[0];
                    outPath = args[1];
                }else{
                    if(args.length==3){
                        inPath = args[0];
                        outPath = args[1];
                        ast = true;
                    }
                }
            }
            if(ast){
                ConvertAST convertAST = new ConvertAST();
                convertAST.run(inPath, outPath);
            }else{
                ConvertGroum converterGroum = new ConvertGroum();
                converterGroum.run(inPath, outPath);
            }
        } catch (Exception e) { System.out.println("input error "+e);}
    }
}
