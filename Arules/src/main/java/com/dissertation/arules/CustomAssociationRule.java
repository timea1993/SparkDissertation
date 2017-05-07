package com.dissertation.arules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CustomAssociationRule implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> consequent;
	private List<String> antecendent;
	private double confidence;
	private double chisquare;

	public boolean removable;

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
		if (rule.equals(this)) {
			removable = false;
			return removable;
		}
		if (this.antecendent.containsAll(rule.antecendent) && this.consequent.containsAll(rule.consequent))
			removable = true;
		else
			removable = false;
		return removable;

	}

	@Override
	public int hashCode() {
		int result = 0;
		for (int i = 0; i < antecendent.size(); i++) {
			result += 37 * result + antecendent.get(i).hashCode();
		}
		for (int i = 0; i < consequent.size(); i++) {
			result += 37 * result + consequent.get(i).hashCode();

		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof CustomAssociationRule) {
			final CustomAssociationRule other = (CustomAssociationRule) o;
			if (other.antecendent.size() != this.antecendent.size()
					|| other.consequent.size() != this.consequent.size())
				return false;
			if (other.antecendent.containsAll(this.antecendent) && other.consequent.containsAll(this.consequent)) {
				return true;
			}
		}
		return false;

	}

	public boolean removable() {
		return this.removable;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		// builder.append("Rule = [ ");
		builder.append(this.antecendent.toString());
		builder.append(" = > ");
		builder.append(this.consequent.toString());
		if (this.removable()) {
			builder.append(" - Flagged ");
		}
		builder.append(", confidence = ");
		builder.append(this.confidence);
		if (chisquare != 0) {
			builder.append(", chisquare = ");
			builder.append(this.chisquare);
		}
		// builder.append(" ]");
		return builder.toString();
	}
}
