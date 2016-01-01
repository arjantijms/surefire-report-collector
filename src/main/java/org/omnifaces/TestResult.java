package org.omnifaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TestResult {

    private String module;
    private String testMethod;
    private String testClass;
    private boolean failure;
    private String failureReason = "";

    @XmlElement
    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @XmlElement
    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    @XmlElement
    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    @XmlElement
    public boolean isFailure() {
        return failure;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    @XmlElement
    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}