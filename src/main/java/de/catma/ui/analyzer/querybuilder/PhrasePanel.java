/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.catma.ui.analyzer.querybuilder;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

import de.catma.document.Corpus;
import de.catma.queryengine.QueryOptions;
import de.catma.queryengine.querybuilder.QueryTree;
import de.catma.ui.dialog.wizard.ToggleButtonStateListener;

public class PhrasePanel extends AbstractSearchPanel {
	
	private Panel wordSequencePanel;
	private List<WordPanel> wordPanels;
	private Button btAddWordPanel;
	private ResultPanel resultPanel;

	public PhrasePanel(
			ToggleButtonStateListener toggleButtonStateListener,
			QueryTree queryTree,
			QueryOptions queryOptions,
			Corpus corpus) {
		super(toggleButtonStateListener, queryTree, queryOptions, corpus);
		wordPanels = new ArrayList<WordPanel>();
		initComponents();
		initActions();
	}

	private void initActions() {
		btAddWordPanel.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				WordPanel wordPanel = 
						new WordPanel(true, wordPanels, new ValueChangeListener() {
							
							public void valueChange(ValueChangeEvent event) {
								showInPreviw();
							}
						});
				addWordPanel(wordPanel);
			}
		});
		resultPanel.addBtShowInPreviewListener(new ClickListener() {
			public void buttonClick(ClickEvent event) {
				showInPreviw();
			}
		});
	}

	private void showInPreviw() {
		StringBuilder builder = new StringBuilder();
		String conc=""; //$NON-NLS-1$
		for (WordPanel panel : wordPanels) {
			builder.append(conc);
			builder.append(panel.getWildcardWord());
			conc = " "; //$NON-NLS-1$
		}
		
		if (!builder.toString().trim().isEmpty()) {
			if (curQuery != null) {
				queryTree.removeLast();
			}
			curQuery = "wild=\"" + builder.toString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			resultPanel.setQuery(curQuery);
			queryTree.add(curQuery);
			onFinish = !isComplexQuery();
			onAdvance = true;
			toggleButtonStateListener.stepChanged(this);
		}
		else {
			onFinish = false;
			onAdvance = false;
		}
		
	}

	private void initComponents() {
		VerticalSplitPanel splitPanel = new VerticalSplitPanel();
		Component searchPanel = createSearchPanel();
		splitPanel.addComponent(searchPanel);
		resultPanel = new ResultPanel(queryOptions);
		splitPanel.addComponent(resultPanel);
		addComponent(splitPanel);
		super.initSearchPanelComponents(splitPanel);
	}

	private Component createSearchPanel() {
		VerticalLayout searchPanel = new VerticalLayout();
		searchPanel.setSpacing(true);
		
		wordSequencePanel = new Panel(new HorizontalLayout());
		searchPanel.addComponent(wordSequencePanel);
		
		WordPanel firstWordPanel = new WordPanel(new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				showInPreviw();
			}
		});
		addWordPanel(firstWordPanel);
		
		HorizontalLayout buttonPanel = new HorizontalLayout();
		
		Label addWordPanelLabel = 
				new Label(Messages.getString("PhrasePanel.PhraseContainsMoreWords")); //$NON-NLS-1$
		buttonPanel.addComponent(addWordPanelLabel);
		buttonPanel.setComponentAlignment(
				addWordPanelLabel, Alignment.MIDDLE_CENTER);
		
		btAddWordPanel = new Button(Messages.getString("PhrasePanel.AddAnotherWord")); //$NON-NLS-1$
		buttonPanel.addComponent(btAddWordPanel);
		searchPanel.addComponent(buttonPanel);
		
		return searchPanel;
	}


	private void addWordPanel(WordPanel wordPanel) {
		((HorizontalLayout)wordSequencePanel.getContent()).addComponent(wordPanel);
		wordPanels.add(wordPanel);
	}
	
	@Override
	public String getCaption() {
		return Messages.getString("PhrasePanel.PhraseLookLike"); //$NON-NLS-1$
	}

	@Override
	public void attach() {
		super.attach();
		getParent().setHeight("100%"); //$NON-NLS-1$
	}
	
	
	@Override
	public String toString() {
		return Messages.getString("PhrasePanel.ByWordsOrPhrase"); //$NON-NLS-1$
	}

}
