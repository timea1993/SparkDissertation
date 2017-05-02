package com.dissertation.arules;

import java.util.Set;

public class ContingencyCell {
        private int binaryValue;
        private Set<String> containedValues;
        private Set<String> negatedValues;
		public Set<String> getContainedValues() {
			return containedValues;
		}
		public void setContainedValues(Set<String> containedValues) {
			this.containedValues = containedValues;
		}
		public Set<String> getNegatedValues() {
			return negatedValues;
		}
		public void setNegatedValues(Set<String> negatedValues) {
			this.negatedValues = negatedValues;
		}
		public int getBinaryValue() {
			return binaryValue;
		}
		public void setBinaryValue(int binaryValue) {
			this.binaryValue = binaryValue;
		}
}
