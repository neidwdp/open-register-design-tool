/*
 * Copyright (c) 2016 Juniper Networks, Inc. All rights reserved. 
 */
package ordt.extract.model;

import ordt.extract.Ordt;
import ordt.extract.RegNumber;
import ordt.output.OutputBuilder;

public class ModRootComponent extends ModComponent {

	public ModRootComponent () {
		setId("root");  // set root name
    	setRoot(true);  // tag root component
	}

	/** remove all root instance children */
	public void removeChildInstances() {
		getChildInstances().clear();
	}

	/** compute min size of all instanced subcomponents */  
	public void setAlignedSize() {
        // only use first instance for size computation (assumes no addr/shift/mod on root inst)
		ModInstance inst = this.getFirstChildInstance();
		if (inst != null) {
			inst.regComp.setAlignedSize();  //recursively set sizes
			this.alignedSize = new RegNumber(inst.regComp.getAlignedSize());  // root comp size is same as first comp?
		}

		/* add all child sizes
		RegNumber newAlignedSize = new RegNumber(0);
		for (ModInstance regInst : childInstances) {
			regInst.regComp.setAlignedSize();
			newAlignedSize.add(regInst.regComp.getAlignedSize());
		}
		newAlignedSize.setNextHighestPowerOf2();  // round to next power of 2
		this.alignedSize = newAlignedSize;
		*/
	}

	/** sortRegisters - fix simple out of order address order issues */
	@Override
	public void sortRegisters() {
		if (needsAddressSort()) sortChildrenByAddress();
		// now process children
		for (ModInstance regInst : getChildInstances()) regInst.regComp.sortRegisters();
	}

	// ------------------------------------ code gen templates ----------------------------------------

	/* generate output */
	@Override
	public void generateOutput(ModInstance callingInst, OutputBuilder outputBuilder) {

		/*if (callingInst == null)
			   System.out.println("--- RegMap.generateOutput: root address map, null instance");*/


		//System.out.println(RegExtractor.repeat(' ', depth) + "---> regmap,  children=" + childInstances.size() + ",  comp=" + getId()+ ",  null callinginst" + (callingInst == null));//+ callingInst.getId());

		// issue warning if more than one root instance detected
		ModInstance inst = this.getFirstChildInstance();
		if (getChildInstances().size() > 1) {
			Ordt.warnMessage("More than one root instance found in input file. Only first instance ("+ inst.getId() + ") will be processed.");
		}
		
		// recursively generate output
		if (inst != null) inst.generateOutput(outputBuilder);

		/* generate each direct instance in this component
		for (ModInstance regInst : childInstances) {
			regInst.generateOutput(outputBuilder);
		}*/

	}

}
