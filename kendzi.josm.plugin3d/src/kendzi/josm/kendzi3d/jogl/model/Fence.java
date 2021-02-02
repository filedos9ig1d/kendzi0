/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.jogl.model;

import java.util.List;

import javax.media.opengl.GL2;

import kendzi.jogl.model.factory.MaterialFactory;
import kendzi.jogl.model.factory.MeshFactory;
import kendzi.jogl.model.factory.ModelFactory;
import kendzi.jogl.model.geometry.Material;
import kendzi.jogl.model.geometry.Model;
import kendzi.jogl.model.render.ModelRender;
import kendzi.josm.kendzi3d.jogl.Camera;
import kendzi.josm.kendzi3d.jogl.ModelUtil;
import kendzi.josm.kendzi3d.jogl.model.clone.RelationCloneHeight;
import kendzi.josm.kendzi3d.jogl.model.tmp.AbstractWayModel;
import kendzi.josm.kendzi3d.service.MetadataCacheService;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Fence for shapes defined as way.
 *
 * @author Tomasz Kędziora (Kendzi)
 */
public class Fence extends AbstractWayModel {

    /** Log. */
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(Fence.class);

    private static final java.lang.Double FENCE_HEIGHT = 1d;

    /**
     * Hight.
     */
    private double hight;

    /**
     * Min height.
     */
    private double minHeight;

    /**
     * Model of building.
     */
    private Model model;


    /**
     * Renderer of model.
     */
    private ModelRender modelRender;

    /**
     * Height cloner.
     */
    private List<RelationCloneHeight> heightClone;


    /**
     * Fence constructor.
     *
     * @param pWay way
     * @param pers Perspective
     */
    public Fence(Way pWay, Perspective3D pers, ModelRender pModelRender) {
        super(pWay, pers);

        this.modelRender = pModelRender;
    }


    @Override
    public void buildModel() {

        if (!(this.points.size() > 1)) {
            return;
        }

        String fenceType = FenceRelation.getFenceType(this.way);

        MetadataCacheService metadataCacheService = getMetadataCacheService();

        double fenceHeight = metadataCacheService.getPropertitesDouble(
                "barrier.fence_{0}.height", FENCE_HEIGHT, fenceType);

        this.hight = ModelUtil.getHeight(this.way, fenceHeight);

        this.minHeight = ModelUtil.getMinHeight(this.way, 0d);


        ModelFactory modelBuilder = ModelFactory.modelBuilder();
        MeshFactory meshBorder = modelBuilder.addMesh("fence_border");

        TextureData facadeTexture = FenceRelation.getFenceTexture(fenceType, this.way, metadataCacheService);
        Material fenceMaterial = MaterialFactory.createTextureMaterial(facadeTexture.getFile());

        int facadeMaterialIndex = modelBuilder.addMaterial(fenceMaterial);

        meshBorder.materialID = facadeMaterialIndex;
        meshBorder.hasTexture = true;


        FenceRelation.buildWallModel(this.points, null, this.minHeight, this.hight, 0, meshBorder, facadeTexture);


        this.model = modelBuilder.toModel();
        this.model.setUseLight(true);
        this.model.setUseTexture(true);

        this.buildModel = true;

        this.heightClone = RelationCloneHeight.buildHeightClone(this.way);
    }


    @Override
    public void draw(GL2 pGl, Camera pCamera) {


        // do not draw the transparent parts of the texture
        pGl.glEnable(GL2.GL_BLEND);
        pGl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        // don't show source alpha parts in the destination

        // determine which areas of the polygon are to be rendered
        pGl.glEnable(GL2.GL_ALPHA_TEST);
        pGl.glAlphaFunc(GL2.GL_GREATER, 0); // only render if alpha > 0

        // replace the quad colours with the texture
        //      gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
        pGl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);




        pGl.glEnable(GL2.GL_CULL_FACE);

        pGl.glPushMatrix();
        pGl.glTranslated(this.getGlobalX(), 0, -this.getGlobalY());

        //pGl.glColor3f((float) 188 / 255, (float) 169 / 255, (float) 169 / 255);

        try {
            this.modelRender.render(pGl, this.model);

            for (RelationCloneHeight cloner : this.heightClone) {
                for (Double height : cloner) {

                    pGl.glPushMatrix();
                    pGl.glTranslated(0, height, 0);

                    this.modelRender.render(pGl, this.model);
                    pGl.glPopMatrix();

                }
            }

        } finally {

            pGl.glPopMatrix();

            pGl.glDisable(GL2.GL_CULL_FACE);
        }
    }
}
