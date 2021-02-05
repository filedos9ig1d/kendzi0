/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.jogl.model.roof;

import java.util.Map;

import kendzi.jogl.model.render.ModelRender;
import kendzi.josm.kendzi3d.jogl.model.Building;
import kendzi.josm.kendzi3d.jogl.model.Perspective3D;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.DormerRoofBuilder;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.Parser;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofOutput;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofTextureData;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.model.DormerRoofModel;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.type.alias.RoofTypeAliasEnum;
import kendzi.josm.kendzi3d.service.MetadataCacheService;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.osm.Way;

/**
 * Represent flat roof.
 *
 * @author Tomasz Kedziora (Kendzi)
 */
public class ShapeRoof extends DormerRoof {

    /** Log. */
    private static final Logger log = Logger.getLogger(ShapeRoof.class);

    /** Flat Roof.
     * @param pBuilding building
     * @param pWay way
     * @param pPerspective perspective
     * @param pModelRender model render
     * @param pMetadataCacheService metadata cache service
     */
    public ShapeRoof(Building pBuilding, Way pWay, Perspective3D pPerspective,
            ModelRender pModelRender, MetadataCacheService pMetadataCacheService) {
        super(pBuilding, pWay, pPerspective, pModelRender, pMetadataCacheService);
    }




    @Override
    public void buildModel() {

        Map<String, String> keys = this.way.getKeys();


        DormerRoofModel roof = parseDormerRoof(keys);

        String shapeName = keys.get("building:roof:shape");
        if (shapeName == null) {
            shapeName = keys.get("roof:shape");
        }

        RoofTypeAliasEnum shape = Parser.parseRoofShape(shapeName);
        if (shape == null) {
            shape = RoofTypeAliasEnum.FLAT;
        }

        roof.setRoofType(Parser.parseRoofType(shape.getKey()));

        RoofTextureData rtd = new RoofTextureData();
        rtd.setFacadeTextrure(getFasadeTexture());
        rtd.setRoofTexture(getRoofTexture());


        RoofOutput roofOutput = DormerRoofBuilder.build(roof, this.height, rtd);

        this.debug = roofOutput.getDebug();

        this.minHeight = this.height - roofOutput.getHeight();
        this.model = roofOutput.getModel();

    }
}
