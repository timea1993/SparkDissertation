package com.dissertation.util;

import java.io.Serializable;

import org.apache.spark.api.java.JavaSparkContext;

public class Config implements Serializable {
	private Double minSupport = 0.005;// set default value
	private Double minConfidence = 0.8; // set default value
	private String inputFile;
	private String outputFile;

	public Config(JavaSparkContext sc) {
		try {
			minSupport = Double.parseDouble(sc.getConf().get("spark.ArulesApp.minSup").trim());
			minConfidence = Double.parseDouble(sc.getConf().get("spark.ArulesApp.minConf").trim());

		} catch (NumberFormatException ex) { // handle your exception
			throw new RuntimeException();
		}
		setInputFile(sc.getConf().get("spark.ArulesApp.input").trim());
		setOutputFile(sc.getConf().get("spark.ArulesApp.output").trim());
	}

	public Double getMinSupport() {
		return minSupport;
	}

	public Double getMinConfidence() {
		return minConfidence;
	}

	public void setMinConfidence(Double minConfidence) {
		this.minConfidence = minConfidence;
	}

	public String getInputFile() {
		return inputFile;
	}

	private void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	private void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}
