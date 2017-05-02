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

import scala.Tuple2;

public class Main {
	private final static Logger logger = Logger.getLogger("myLogger");

	public static void arules(double minSupport, double minConfidence) {
		SparkConf conf = new SparkConf().setMaster("local").setAppName("Arules");
		JavaSparkContext sc = new JavaSparkContext(conf);

		JavaRDD<String> data = sc.textFile("E:\\SPARK\\input\\titanic.txt");

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

		FPGrowth fpg = new FPGrowth().setMinSupport(minSupport).setNumPartitions(10);

		System.out.println("-------------------------");
		System.out.println("Finding frequent itemsets: ");
		System.out.println("-------------------------");
		FPGrowthModel<String> model = fpg.run(transactions);

		try {

			PrintWriter writer = new PrintWriter("E:\\SPARK\\output\\output_titanic_chisq.txt", "UTF-8");
			writer.println("//Finding association rules with minSupport = " + minSupport + " and minConfidence = "
					+ minConfidence + "\n");
			List<FreqItemset<String>> allItems = model.freqItemsets().toJavaRDD().collect();

			for (FPGrowth.FreqItemset<String> itemset : allItems) {

				writer.println("[" + itemset.javaItems() + "], " + itemset.freq());

			}
			writer.println("\n");
			System.out.println("-------------------------");
			System.out.println("Finding association rules from the frequent itemsets: ");
			System.out.println("-------------------------");
			/*
			 * JavaRDD<Rule<String>> allRules =
			 * model.generateAssociationRules(minConfidence).toJavaRDD();
			 * allRules.foreach(new VoidFunction<Rule<String>>() {
			 * 
			 *//**
				* 
				*//*
				 * private static final long serialVersionUID = 1L;
				 * 
				 * @Override public void call(Rule<String> rule) throws
				 * Exception { List<String> ruleBasket = new
				 * ArrayList<String>();
				 * ruleBasket.addAll(rule.javaConsequent());
				 * ruleBasket.addAll(rule.javaAntecedent()); logger.info(
				 * 
				 * rule.javaAntecedent() + " => " + rule.javaConsequent() +
				 * ", confidence = " + rule.confidence());
				 * 
				 * double[] matrix = filterItem(ruleBasket, transactions);
				 * logger.info("Contingency matrix (!!column-wise traversal) = "
				 * ); for (int i = 0; i < matrix.length; i++)
				 * logger.info(matrix[i] + " ");
				 * logger.info(computeChisq(matrix,
				 * rule.javaAntecedent().size()));
				 * 
				 * logger.info("\n");
				 * 
				 * } });
				 */
			for (AssociationRules.Rule<String> rule : model.generateAssociationRules(minConfidence).toJavaRDD()
					.collect()) {

				List<String> ruleBasket = new ArrayList<String>();
				ruleBasket.addAll(rule.javaConsequent());
				ruleBasket.addAll(rule.javaAntecedent());
				writer.println(

						rule.javaAntecedent() + " => " + rule.javaConsequent() + ", confidence = " + rule.confidence());

				double[] matrix = filterItem(ruleBasket, transactions);
				writer.print("Contingency matrix (!!column-wise traversal) = ");
				for (int i = 0; i < matrix.length; i++)
					writer.print(matrix[i] + " ");
				writer.print(computeChisq(matrix, rule.javaAntecedent().size()));

				writer.println("\n");

			}
			
		
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			sc.close();
		}

	}

	/**
	 * @param -
	 *            input the contingency matrix, which should be specified in
	 *            column traversal order !A!B!C->000=0, !AB!C->010=2,
	 *            A!B!C->100=4, AB!C->110=6, !A!BC->001=1, !ABC->011=3,
	 *            A!BC->101=5, ABC->111=7,
	 * @param size
	 *            - the size of the antecedent of a rule
	 * @return
	 */
	public static ChiSqTestResult computeChisq(double[] input, int size) {

		// Create a contingency matrix
		// The matrix should be specified with column traversal as double[]

		int col = (int) Math.pow(2, size);
		int row = 2;
		Matrix mat = Matrices.dense(row, col, input);

		// conduct Pearson's independence test on the input contingency matrix
		ChiSqTestResult independenceTestResult = Statistics.chiSqTest(mat);

		return independenceTestResult;

	}
     
	/**
	 * @param basket
	 *            list of elements containing the items from an association rule
	 * @param transactions
	 *            list of the lines from the input (no of baskets)
	 * @return
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static double[] filterItem(List<String> basket, JavaRDD<List<String>> transactions)
			throws FileNotFoundException, UnsupportedEncodingException {
		// Compute the number of possible combinations for an association rule:
		// {A,B}->{C} we will have
		// !A!B!C->000=0,!A!BC->001=1,!AB!C->010=2,!ABC->011=3,
		// A!B!C->100=4,A!BC->101=5, AB!C->110=6, ABC->111=7,

		int combinations = (int) Math.pow(2, basket.size());
		double[] result = new double[combinations];
		for (int i = 0; i < combinations; i++) {
			Set<String> in = new HashSet<String>();
			Set<String> out = new HashSet<String>();

			for (int bit = 0; bit < basket.size(); bit++) {

				if (BigInteger.valueOf(i).testBit(bit)) {
					in.add(basket.get(bit));
				} else {
					out.add(basket.get(bit));
				}
			}

			JavaRDD<List<String>> countItems = transactions.filter(new Function<List<String>, Boolean>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public Boolean call(List<String> line) {
					Set<String> intersect = new HashSet<String>(line);
					intersect.retainAll(out);
					return line.containsAll(in) && intersect.size() == 0;
				}

			});

			result[i] = countItems.count() == 0 ? 1 : countItems.count();

		}
		return result;

	}

	public static void run() {
		double minSupport = 0.005;
		double minConfidence = 0.8;
		SparkConf conf = new SparkConf().setMaster("local").setAppName("Arules");
		JavaSparkContext sc = new JavaSparkContext(conf);

		JavaRDD<String> data = sc.textFile("E:\\SPARK\\input\\titanic.txt");

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
		AssociationRuleMiner miner = new AssociationRuleMiner(minSupport, minConfidence);
		miner.getAssociationRules(transactions);
		sc.close();
	}

	public static void filterSuperSets() {

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
			/*
			 * SparkConf conf = new
			 * SparkConf().setMaster("local").setAppName("Test");
			 * JavaSparkContext sc = new JavaSparkContext(conf);
			 * 
			 * List<String> basket = Arrays.asList("A", "B"); List<String> listA
			 * = Collections.nCopies(5, "A"); List<String> listB =
			 * Collections.nCopies(5, "B"); List<String> listC =
			 * Collections.nCopies(10, "C"); List<String> listAB = new
			 * ArrayList<String>(); listAB.addAll(listA); listAB.addAll(listB);
			 * 
			 * for (int i = 0; i < 100; i++) { listA.add("A"); }
			 * JavaRDD<List<String>> transactions =
			 * sc.parallelize(Arrays.asList(listB, listC, listAB));
			 * 
			 * for (List<String> list : transactions.collect()) {
			 * System.out.println(list.toString()); } double[] result =
			 * Main.filterItem(basket, transactions); for (int i = 0; i <
			 * result.length; i++) { System.out.println(result[i]); }
			 */
			//double minSupport = 0.005;
			//double minConfidence = 0.8;
			//arules(minSupport, minConfidence);
			 run();

		}
	}

}