package org.omnifaces;

import static java.lang.System.out;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import static org.w3c.dom.Node.ELEMENT_NODE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateReport {

	private static final DocumentBuilder documentBuilder = createDocumentBuilder();

	public static void main(String[] args) throws IOException {

		String rootPath = args != null && args.length > 0 ? args[0] : ".";
		String outputPath = args != null && args.length > 1 ? args[1] : "./test-results.xml";
		boolean clean = outputPath.equals("-clean");
		
		if (!outputPath.endsWith(".xml")) {
		    outputPath += ".xml";
		}
		
		String testName = args != null && args.length > 2 ? args[2] : "";

		out.println("Scanning from " + rootPath + " = " + get(rootPath).toAbsolutePath().toRealPath() + "\n");
		if (clean) {
		    out.println("Cleaning up surefire test reports\n");
		} else {
		    out.println("Saving to " + outputPath + " = " + get(outputPath).toAbsolutePath().toFile().getCanonicalPath() + "\n");
		}

		List<TestResult> testResults = new ArrayList<>();

		walk(get(rootPath))
			.filter(path -> fileName(path).startsWith("TEST") && fileName(path).endsWith(".xml") && parentEndsWith(path, "target/surefire-reports"))
			.forEach(
				path -> {
					Element root = document(path).getDocumentElement();

					if (root.getNodeName().equals("testsuite")) {
					    
					    if (clean) {
					        out.println("Deleting " + path);
					        path.toFile().delete();
					        
					        return;
					    }

						forEachElement(root.getElementsByTagName("testcase"),
							element -> {

								TestResult testResult = new TestResult();
								testResult.setModule(fileName(path.getParent().getParent().getParent()));
								testResult.setTestMethod(element.getAttribute("name"));
								testResult.setTestClass(element.getAttribute("classname"));

								forEachElement(element.getElementsByTagName("failure"),
									failureElement -> {
										testResult.setFailure(true);
										testResult.setFailureReason(failureElement.getAttribute("message"));
										return;
									}
								);

								testResults.add(testResult);

								return;
						});
					}

					return;
				}
		);
		
		if (clean) {
		    out.println("Done\n");
		    return;
		}

		for (TestResult testResult : testResults) {
			out.format("%s %s %s %s \n",
				testResult.getModule(),
				testResult.getTestMethod(),
				testResult.isFailure() ? "Failure:" : "Passed",
				testResult.getFailureReason()
			);
		}

        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(TestResults.class, TestResult.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(new TestResults(testName, testResults), get(outputPath).toFile());

        } catch (JAXBException e) {
            e.printStackTrace();
        }

	}

	public static String fileName(Path path) {
		return path != null && path.getFileName() != null ? path.getFileName().toString() : "";
	}

	public static boolean parentEndsWith(Path path, String other) {
		return path.getParent() != null && path.getParent().endsWith(other);
	}

	public static DocumentBuilder createDocumentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document document(Path path) {
		try {
			Document document = documentBuilder.parse(newInputStream(path));
			document.getDocumentElement().normalize();
			return document;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void forEachElement(NodeList nodes, Consumer<Element> consumer) {
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == ELEMENT_NODE) {
				consumer.accept((Element) node);
			}
		}
	}



}