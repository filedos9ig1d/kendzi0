/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.action;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonModel;

import kendzi.jogl.model.render.ModelRender;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

import com.google.inject.Inject;

/**
 * Debug toggle action.
 *
 * @author Tomasz Kędziora (Kendzi)
 *
 */
public class DebugToggleAction extends JosmAction {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Debug view property key.
     */
    public final static String KENDZI_3D_DEBUG_VIEW = "kendzi3d.debug.view";

    /**
     * Button models.
     */
    private final List<ButtonModel> buttonModels = new ArrayList<ButtonModel>();
    //FIXME: replace with property Action.SELECTED_KEY when migrating to
    // Java 6
    private boolean selected;

    /**
     * Model render.
     */
    private ModelRender modelRender;

    /**
     * Constructor of debug toggle action.
     * @param pModelRender model render
     */
    @Inject
    public DebugToggleAction(ModelRender pModelRender) {
        super(
                tr("Debug View"),
                "1306318261_debugger__24",
                tr("Enable/disable display debug information"),
                null,
                false
        );

        this.selected = Main.pref.getBoolean(KENDZI_3D_DEBUG_VIEW, false);

        this.modelRender = pModelRender;
        // Main.pref.getBoolean("draw.wireframe", false);
        notifySelectedState();

        setDebugMode(this.selected);
    }

    /**
     * @param pModel button model
     */
    public void addButtonModel(ButtonModel pModel) {
        if (pModel != null && !this.buttonModels.contains(pModel)) {
            this.buttonModels.add(pModel);
            pModel.setSelected(this.selected);
        }
    }

    /**
     * @param pModel button model
     */
    public void removeButtonModel(ButtonModel pModel) {
        if (pModel != null && this.buttonModels.contains(pModel)) {
            this.buttonModels.remove(pModel);
        }
    }

    /**
     *
     */
    protected void notifySelectedState() {
        for (ButtonModel model : this.buttonModels) {
            if (model.isSelected() != this.selected) {
                model.setSelected(this.selected);
            }
        }
    }

    /**
     *
     */
    protected void toggleSelectedState() {
        this.selected = !this.selected;
        Main.pref.put(KENDZI_3D_DEBUG_VIEW, this.selected);
        notifySelectedState();

        setDebugMode(this.selected);
    }

    /**
     * @param pEnable enable debug
     */
    private void setDebugMode(boolean pEnable) {

        this.modelRender.setDebugging(pEnable);
        this.modelRender.setDrawEdges(pEnable);
        this.modelRender.setDrawNormals(pEnable);

    }

    @Override
    public void actionPerformed(ActionEvent pE) {
        toggleSelectedState();
    }

    @Override
    protected void updateEnabledState() {
//        setEnabled(Main.map != null && Main.main.getEditLayer() != null);
    }

    /** If can be in debug mode.
     * @return debug mode
     */
    public boolean canDebug() {
        return true;
    }
}
