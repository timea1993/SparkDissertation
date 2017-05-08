package com.dissertation.arules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.fpm.AssociationRules;
import org.apache.spark.mllib.fpm.AssociationRules.Rule;
import org.apache.spark.mllib.fpm.FPGrowth;
import org.apache.spark.mllib.fpm.FPGrowthModel;
import org.apache.spark.mllib.fpm.FPGrowth.FreqItemset;
import org.apache.spark.mllib.linalg.Matrices;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.stat.Statistics;
import org.apache.spark.mllib.stat.test.ChiSqTestResult;

import com.dissertation.util.Config;

import scala.Tuple2;

public class AssociationRuleMiner implements Serializable {
	private final static Logger logger = Logger.getLogger("myLogger");
	private double minSupport;
	private double minConfidence;
	private FPGrowth fpg;
	private Config config;

	public AssociationRuleMiner(Config config) {
		this.config = config;
		this.minSupport = config.getMinSupport();
		this.minConfidence = config.getMinConfidence();
		this.fpg = new FPGrowth().setMinSupport(minSupport).setNumPartitions(10);
		logger.info("Finding association rules with minSupport = " + minSupport + " and minConfidence = "
				+ minConfidence + "\n");
	}

	public void getAssociationRules(JavaRDD<List<String>> transactions)
			throws FileNotFoundException, UnsupportedEncodingException {

		FPGrowthModel<String> model = fpg.run(transactions);

		// List<FreqItemset<String>> allItems =
		// model.freqItemsets().toJavaRDD().collect();

		final PrintWriter writer = new PrintWriter(config.getOutputFile(), "UTF-8");
		writer.println();
		writer.println("//Finding association rules with minSupport = " + minSupport + " and minConfidence = "
				+ minConfidence + "\n");

		/*
		 * for (FPGrowth.FreqItemset<String> itemset : allItems) {
		 * 
		 * logger.info("[" + itemset.javaItems() + "], " + itemset.freq());
		 * writer.println("[" + itemset.javaItems() + "], " + itemset.freq()); }
		 */
		JavaRDD<Rule<String>> rules = model.generateAssociationRules(minConfidence).toJavaRDD();

		JavaRDD<CustomAssociationRule> result = rules.map((new Function<Rule<String>, CustomAssociationRule>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public CustomAssociationRule call(Rule<String> rule) throws Exception {
				CustomAssociationRule customRule = new CustomAssociationRule();

				List<String> ruleBasket = new ArrayList<String>();
				ruleBasket.addAll(rule.javaConsequent());
				ruleBasket.addAll(rule.javaAntecedent());
				customRule.getAntecendent().addAll(rule.javaAntecedent());
				customRule.getConsequent().addAll(rule.javaConsequent());
				customRule.setConfidence(rule.confidence());
				// logger.info("Contingency matrix (!!column-wise traversal) =
				// ");
				// double[] matrix = filterItem(ruleBasket, transactions);
				// customRule.setChisquare(computeChisq(matrix,
				// rule.javaAntecedent().size()).statistic());
				return customRule;

			}

		}));
		writer.println("************************************");

		writer.println("Before filtering:  " + result.collect().size() + " rules" + "\n");
		logger.info("Before filtering " + result.collect().size() + " rules");

		for (CustomAssociationRule rule : result.collect()) {
			writer.println(rule.toString());
		}
		JavaRDD<CustomAssociationRule> toFilter = result.cartesian(result).map(SuperSetFilter.mapSuperSets)
				.filter(SuperSetFilter.retainSuperSets).distinct();
		JavaRDD<CustomAssociationRule> filteredRDD = result.subtract(toFilter);
		List<CustomAssociationRule> finalResult = filteredRDD.collect();

		logger.info("After filtering there will be " + finalResult.size() + " rules");
		writer.println("************************************");
		writer.println(
				"Printing out the rules after filtering the supersets:  " + finalResult.size() + " rules " + "\n");

		for (CustomAssociationRule rule : finalResult) {
			writer.println(rule.toString());
		}
		
	/*	JavaRDD<CustomAssociationRule> resultwithChisq = filteredRDD.map((new Function<CustomAssociationRule, CustomAssociationRule>() {

			public CustomAssociationRule call(CustomAssociationRule rule) throws Exception {
				List<String> ruleBasket = new ArrayList<String>();
				ruleBasket.addAll(rule.getConsequent());
				ruleBasket.addAll(rule.getAntecendent());
				double[] matrix = filterItem(ruleBasket, transactions);
				rule.setChisquare(computeChisq(matrix, rule.getAntecendent().size()).statistic());
		
				return rule;

			}

		}));
		resultwithChisq.collect();*/
		logger.info("Computing chisquare for each rule");
		writer.println("************************************");
		
		writer.println("Computing chisquare for each rule: " + "\n");
		
		for (CustomAssociationRule rule : finalResult) {
			List<String> ruleBasket = new ArrayList<String>();
			ruleBasket.addAll(rule.getConsequent());
			ruleBasket.addAll(rule.getAntecendent());
			double[] matrix = filterItem(ruleBasket, transactions);
			rule.setChisquare(computeChisq(matrix, rule.getAntecendent().size()).statistic());
		}
		
		for (CustomAssociationRule rule : finalResult) {
			writer.println(rule.toString());
		}
		logger.info("Finishing chisquare");
		writer.close();
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
	public static double[] filterItem(List<String> basket, JavaRDD<List<String>> transactions) {
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
}
