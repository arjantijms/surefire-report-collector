package org.omnifaces;

import static java.lang.System.out;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.util.Comparator.comparing;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class RenderJoinedReports {

    public static void main(String[] args) throws IOException, JAXBException {
        
        String rootPath = args != null && args.length > 0 ? args[0] : ".";

        out.println("Scanning from " + rootPath + " = " + get(rootPath).toAbsolutePath().toRealPath() + "\n");
        
        JAXBContext jaxbContext = JAXBContext.newInstance(TestResults.class, TestResult.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        List<TestResults> allTestResults = new ArrayList<>();

        walk(get(rootPath), 1)
            .filter(path -> fileName(path).endsWith(".xml"))
            .forEach(
                path -> {
                    
                    try {
                        out.print("Reading " + path);
                        TestResults results = (TestResults) jaxbUnmarshaller.unmarshal(path.toFile());
                        
                        Comparator<TestResult> byModule = comparing(TestResult::getModule);
                        Comparator<TestResult> byTest =  comparing(TestResult::getTestClass);
                        
                        results.getResults().sort(byModule.thenComparing(byTest));
                        
                        allTestResults.add(results);
                        
                        System.out.print(" - added results for " + results.getName());
        
                    } catch (JAXBException e) {
                        out.print(" - no test results file ");
                    }
        
                    out.print("\n");
        
                    return;
                }
        );
        
        out.println("\nDone reading, processing...\n");
        
        if (!allTestResults.isEmpty()) {
            
            String baseUrl = "https://github.com/javaee-samples/javaee7-samples/blob/master/jaspic/";
            String testPath = "/src/test/java/";
            String extension = ".java";
            
            out.println("<tr style=\"background-color:LightGray\">");
            out.println("    <th>Module</th> <th>Test</th>");
            
            for (TestResults testResults : allTestResults) {
                out.format("    <th>%s</th>\n",
                    testResults.getName()
                );
            }
            
            out.println("</tr>\n");
            
            for (int i=0; i<allTestResults.get(0).getResults().size(); i++) {
                
                TestResult testResult = allTestResults.get(0).getResults().get(i);

                String moduleUrl = String.format("%s%s",
                    baseUrl,
                    testResult.getModule()
                );

                String classUrl = String.format("%s%s%s%s",
                    moduleUrl,
                    testPath,
                    testResult.getTestClass().replace('.', '/'),
                    extension

                );

                String moduleAnchorTag = String.format("<a href=\"%s\">%s</a>",
                    moduleUrl,
                    testResult.getModule()
                );

                String testAnchorTag = String.format("<a href=\"%s\">%s</a>",
                    classUrl,
                    testResult.getTestMethod()
                );
                
                out.println("<tr>");

                out.format("    <td>%s</td> <td>%s</td>\n",
                    moduleAnchorTag,
                    testAnchorTag
                );
                
                for (TestResults testResults : allTestResults) {
                    TestResult result = testResults.getResults().get(i);

                    if (result.getModule().equals(testResult.getModule()) && result.getTestMethod().equals(testResult.getTestMethod())) {
                        out.format("    <td bgcolor=\"%s\"><div tooltip=\"%s\">%s</div></td>\n",
                            testResults.getResults().get(i).isFailure() ? "LightCoral" : "lightgreen",
                            testResults.getResults().get(i).getFailureReason(),
                            testResults.getResults().get(i).isFailure() ? "Failure" : "Passed"
                        );
                    } else {
                        out.format("<td>ERROR! Result files not compatible!</td>");
                    }
                }
                
                out.println("</tr>\n");
            }
            
        }

    }
    
    public static String fileName(Path path) {
        return path != null && path.getFileName() != null ? path.getFileName().toString() : "";
    }


}
