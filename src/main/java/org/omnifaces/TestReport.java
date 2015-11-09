package org.omnifaces;

import static java.lang.System.out;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static org.w3c.dom.Node.ELEMENT_NODE;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestReport {

	private static final DocumentBuilder documentBuilder = createDocumentBuilder();

	public static void main(String[] args) throws IOException {

		String rootPath = args != null && args.length > 0 ? args[0] : ".";

		out.println("Scanning from " + rootPath + " = " + get(rootPath).toAbsolutePath().toRealPath() + "\n");

		List<TestResult> testResults = new ArrayList<>();

		walk(get(rootPath))

				.filter(path -> fileName(path).startsWith("TEST") && fileName(path).endsWith(".xml")
						&& parentEndsWith(path, "target/surefire-reports"))

				.forEach(

						path -> {

							Element root = document(path).getDocumentElement();

							if (root.getNodeName().equals("testsuite")) {

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

								}

				);

							}

							return;

						}

		);

		for (TestResult testResult : testResults) {

			out.format("%s %s %s %s \n",
					testResult.getModule(),
					testResult.getTestMethod(),
					testResult.isFailure() ? "Failure:" : "Passed",
					testResult.getFailureReason()
			);

		}

		out.println("\n\n");

		String baseUrl = "https://github.com/javaee-samples/javaee7-samples/blob/master/jaspic/";
		String testPath = "/src/test/java/";
		String extension = ".java";

		for (TestResult testResult : testResults) {

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

			out.format("<tr>\n <td>%s</td> <td>%s</td>\n <td bgcolor=\"%s\"><div tooltip=\"%s\">%s</div></td>\n<tr>\n",
					moduleAnchorTag,
					testAnchorTag,
					testResult.isFailure() ? "red" : "green",
					testResult.getFailureReason(),
					testResult.isFailure() ? "Failure" : "Passed"

			);

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

	private static class TestResult {

		private String module;
		private String testMethod;
		private String testClass;
		private boolean failure;
		private String failureReason = "";

		public String getModule() {
			return module;
		}

		public void setModule(String module) {
			this.module = module;
		}

		public String getTestMethod() {
			return testMethod;
		}

		public void setTestMethod(String testMethod) {
			this.testMethod = testMethod;
		}

		public String getTestClass() {
			return testClass;
		}

		public void setTestClass(String testClass) {
			this.testClass = testClass;
		}

		public boolean isFailure() {
			return failure;
		}

		public void setFailure(boolean failure) {
			this.failure = failure;
		}

		public String getFailureReason() {
			return failureReason;
		}

		public void setFailureReason(String failureReason) {
			this.failureReason = failureReason;
		}

	}

}