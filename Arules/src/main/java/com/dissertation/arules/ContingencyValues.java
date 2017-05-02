package com.dissertation.arules;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContingencyValues {
	private CustomAssociationRule rule;
	private List<String> ruleAsBasket;

	public ContingencyValues(CustomAssociationRule rule) {
		this.rule = rule;
		ruleAsBasket = new ArrayList<String>();
		ruleAsBasket.addAll(rule.getConsequent());
		ruleAsBasket.addAll(rule.getAntecendent());
	}

	public void generateCombinations() {
		int combinations = (int) Math.pow(2, this.ruleAsBasket.size());
		Set<String> in = new HashSet<String>();
		Set<String> out = new HashSet<String>();
		for (int i = 0; i < combinations; i++) {
			for (int bit = 0; bit < ruleAsBasket.size(); bit++) {

				if (BigInteger.valueOf(i).testBit(bit)) {
					in.add(ruleAsBasket.get(bit));
				} else {
					out.add(ruleAsBasket.get(bit));
				}
			}
		}
	}

}
