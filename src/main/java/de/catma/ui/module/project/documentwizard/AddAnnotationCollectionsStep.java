package de.catma.ui.module.project.documentwizard;

import com.vaadin.ui.VerticalLayout;

import de.catma.ui.dialog.wizard.ProgressStep;
import de.catma.ui.dialog.wizard.ProgressStepFactory;
import de.catma.ui.dialog.wizard.StepChangeListener;
import de.catma.ui.dialog.wizard.WizardContext;
import de.catma.ui.dialog.wizard.WizardStep;

public class AddAnnotationCollectionsStep extends VerticalLayout implements WizardStep {

	private ProgressStep progressStep;

	public AddAnnotationCollectionsStep(WizardContext wizardContext, ProgressStepFactory progressStepFactory, int stepNo) {
		this.progressStep = progressStepFactory.create(stepNo, "Create Annotation Collections");
	}

	@Override
	public ProgressStep getProgressStep() {
		return progressStep;
	}

	@Override
	public WizardStep getNextStep() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setStepChangeListener(StepChangeListener stepChangeListener) {
		// TODO Auto-generated method stub

	}

}