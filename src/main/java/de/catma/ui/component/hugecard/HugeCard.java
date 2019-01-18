package de.catma.ui.component.hugecard;

import com.vaadin.contextmenu.ContextMenu;

import de.catma.ui.layout.FlexLayout;
import de.catma.ui.util.Styles;

/**
 * Renders one huge card with action bar in the main view section
 *
 * @author db
 */
public class HugeCard extends FlexLayout {

    private final HugeCardBar hugeCardBar;

    public HugeCard(String title) {
        this.hugeCardBar = new HugeCardBar(title);
        initComponents();
    }

   
    private void initComponents() {
        addStyleNames(Styles.hugecard);
        addComponent(hugeCardBar);
    }

    public ContextMenu getBtnMoreOptionsContextMenu() { return hugeCardBar.getBtnMoreOptionsContextMenu(); }

}