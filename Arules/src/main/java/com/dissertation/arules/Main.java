package com.dissertation.arules;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.spark.SparkConf;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.mllib.fpm.AssociationRules;
import org.apache.spark.mllib.fpm.AssociationRules.Rule;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;
import org.apache.spark.mllib.fpm.FPGrowthModel;

import org.apache.spark.mllib.linalg.Matrices;
import org.apache.spark.mllib.linalg.Matrix;

import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.stat.test.ChiSqTestResult;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import com.dissertation.arules.test.TestClass;
import com.dissertation.util.Config;

import scala.Tuple2;

public class Main {
	private final static Logger logger = Logger.getLogger("myLogger");

	public static void run() throws FileNotFoundException, UnsupportedEncodingException {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("Arules");
		JavaSparkContext sc = new JavaSparkContext(conf);

		
		Config config = new Config(sc);
		
		
		JavaRDD<String> data = sc.textFile(config.getInputFile());

		JavaRDD<List<String>> transactions = data.map(new Function<String, List<String>>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public List<String> call(String line) {
				String[] parts = line.split(" ");
				return Arrays.asList(parts);
			}
		});
		AssociationRuleMiner miner = new AssociationRuleMiner(config);
		miner.getAssociationRules(transactions);
		sc.close();
	}

	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		final Logger logger = Logger.getLogger("myLogger");

		if (args.length > 0) {
			String testArg = args[0];
			if ("test".equalsIgnoreCase(testArg)) {
				logger.info("Test case");
				Result result = JUnitCore.runClasses(TestClass.class);

				for (Failure failure : result.getFailures()) {
					System.out.println(failure.toString());
					logger.warn(failure.toString());
				}

				System.out.println(result.wasSuccessful());

			}
		} else {
			logger.warn("Normal case");
			run();

		}
	}

}