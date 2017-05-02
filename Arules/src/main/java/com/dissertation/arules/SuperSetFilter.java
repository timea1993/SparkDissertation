package com.dissertation.arules;

import java.io.Serializable;

import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

public class SuperSetFilter implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Function<CustomAssociationRule, Boolean> retainSuperSets = new Function<CustomAssociationRule, Boolean>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Boolean call(CustomAssociationRule rule) {
			if (rule.removable()) {
				return true;
			}
			// System.out.println(rule + " will be removed");
			return false;
		}
	};

	public static Function<Tuple2<CustomAssociationRule, CustomAssociationRule>, CustomAssociationRule> mapSuperSets = new Function<Tuple2<CustomAssociationRule, CustomAssociationRule>, CustomAssociationRule>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public CustomAssociationRule call(Tuple2<CustomAssociationRule, CustomAssociationRule> arg) throws Exception {

			if (arg._1.isSuperSetOf(arg._2)) {
				arg._1.removable = true;

			}
			return arg._1;
		}

	};

}
