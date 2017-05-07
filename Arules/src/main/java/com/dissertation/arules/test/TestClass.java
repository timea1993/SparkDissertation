package com.dissertation.arules.test;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.stat.test.ChiSqTestResult;
import org.junit.Test;

import com.dissertation.arules.AssociationRuleMiner;
import com.dissertation.arules.CustomAssociationRule;
import com.dissertation.arules.Main;

public class TestClass {
	private static final double DOUBLE_MIN = 0.0001;
	 private final static Logger logger = Logger.getLogger("myLogger");
     
	@Test
	public void testChisq() {
		double[] input = new double[] { 261, 24, 1831, 85 };
		int size = 1;
		ChiSqTestResult chisq = AssociationRuleMiner.computeChisq(input, size);
		double result = chisq.statistic();
		assertEquals(8.3689189911949686, result, DOUBLE_MIN);
		// assertEquals(8, result, Double.MIN_VALUE);

		double[] input2X3 = new double[] { 28.0, 316.0, 17.0, 109.0, 29.0, 338.0, 35.0, 1329.0 };
		int size2X3 = 2;
		ChiSqTestResult chisq2X3 = AssociationRuleMiner.computeChisq(input2X3, size2X3);
		double result2X3 = chisq2X3.statistic();
		assertEquals(50.2304, result2X3, DOUBLE_MIN);

		double[] input2 = new double[] { 45.0, 425.0, 64.0, 1667.0 };
		int size2 = 1;
		ChiSqTestResult chisq2 = AssociationRuleMiner.computeChisq(input2, size2);
		double result2 = chisq2.statistic();
		assertEquals(27.1247196, result2, DOUBLE_MIN);

	}

	@Test
	public void itemCounter() throws FileNotFoundException, UnsupportedEncodingException {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("Test");
		JavaSparkContext sc = new JavaSparkContext(conf);

		List<List<String>> myArr = new ArrayList<List<String>>();
		List<String> basket = Arrays.asList("A", "B");
		List<String> listA = Arrays.asList("A");
		List<String> listB = Arrays.asList("B");
		List<String> listC = Arrays.asList("C");
		List<String> listAB = Arrays.asList("A", "B");
		List<String> listAC = Arrays.asList("A", "C");

		myArr.addAll(Collections.nCopies(5, listA));
		myArr.addAll(Collections.nCopies(20, listB));
		myArr.addAll(Collections.nCopies(1, listC));
		myArr.addAll(Collections.nCopies(10, listAB));
		myArr.addAll(Collections.nCopies(2, listAC));

		JavaRDD<List<String>> transactions = sc.parallelize(myArr);

		double[] result = AssociationRuleMiner.filterItem(basket, transactions);
		for (int i = 0; i < result.length; i++) {
			logger.info(result[i]);
		}
		double[] expectedResult = new double[]{1, 7, 20, 10};
		assertArrayEquals(expectedResult, result, DOUBLE_MIN);
		sc.close();
	}
	@Test
	public void testMethods(){
		CustomAssociationRule rule1 = new CustomAssociationRule();
		CustomAssociationRule rule2 = new CustomAssociationRule();
		CustomAssociationRule rule3 = new CustomAssociationRule();
		
		List<String>antecendent1 = new ArrayList<String>(Arrays.asList("A","B", "C"));
		List<String>consequent1 =new ArrayList<String>(Arrays.asList("C"));
		
		List<String>antecendent2 = new ArrayList<String>(Arrays.asList("A","B"));
		rule1.setAntecendent(antecendent1);
		rule1.setConsequent(consequent1);
		
		rule2.setAntecendent(antecendent2);
		rule2.setConsequent(consequent1);
		
		rule3.setAntecendent(antecendent1);
		rule3.setConsequent(consequent1);
		
		assertEquals(rule1.isSuperSetOf(rule2), true);
		assertEquals(rule2.isSuperSetOf(rule1), false);
		assertEquals(rule3.isSuperSetOf(rule1), false);
		assertEquals(rule3.equals(rule1), true);
	}
}
