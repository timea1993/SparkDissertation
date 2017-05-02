package com.dissertation.arules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.spark.mllib.fpm.AssociationRules.Rule;


public class CustomAssociationRule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
    
	private List<String> consequent;
	private List<String> antecendent;
	private double confidence;
	private double chisquare;

	public void setConsequent(List<String> newConsequent) {
		this.consequent = new ArrayList<String>();
		this.consequent.addAll(newConsequent);
	}

	public void setAntecendent(List<String> newAntecendent) {
		this.antecendent = new ArrayList<String>();
		this.antecendent.addAll(newAntecendent);
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public List<String> getConsequent() {
		if (this.consequent == null) {
			this.consequent = new ArrayList<String>();

		}
		return this.consequent;
	}

	public List<String> getAntecendent() {
		if (this.antecendent == null) {
			this.antecendent = new ArrayList<String>();

		}
		return this.antecendent;
	}

	public double getChisquare() {
		return chisquare;
	}

	public void setChisquare(double chisquare) {
		this.chisquare = chisquare;
	}

	public boolean isSuperSetOf(CustomAssociationRule rule) {
		if (this.antecendent.containsAll(rule.antecendent) && this.consequent.containsAll(rule.consequent))
			return true;
		return false;

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Rule = [ ");
		builder.append(this.antecendent.toString());
		builder.append(" = > ");
		builder.append(this.consequent.toString());
		builder.append(", confidence = ");
		builder.append(this.confidence);
		builder.append(", chisquare = ");
		builder.append(this.chisquare);
		builder.append(" ]");
		return builder.toString();
	}
}
