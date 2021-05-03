/*
 * This software is provided "AS IS" without a warranty of any kind.
 * You use it on your own risk and responsibility!!!
 *
 * This file is shared under BSD v3 license.
 * See readme.txt and BSD3 file for details.
 *
 */

package kendzi.josm.kendzi3d.jogl.model.roof;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL2;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import kendzi.jogl.DrawUtil;
import kendzi.jogl.model.render.ModelRender;
import kendzi.josm.kendzi3d.dto.TextureData;
import kendzi.josm.kendzi3d.jogl.Camera;
import kendzi.josm.kendzi3d.jogl.model.Building;
import kendzi.josm.kendzi3d.jogl.model.Perspective3D;
import kendzi.josm.kendzi3d.jogl.model.attribute.OsmAttributeKeys;
import kendzi.josm.kendzi3d.jogl.model.attribute.OsmAttributeValues;
import kendzi.josm.kendzi3d.jogl.model.export.ExportItem;
import kendzi.josm.kendzi3d.jogl.model.export.ExportModelConf;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.Parser;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofDebugOut;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.model.DormerRoofModel;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.model.RoofTextureData;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.type.alias.RoofTypeAliasEnum;
import kendzi.josm.kendzi3d.service.MetadataCacheService;
import kendzi.josm.kendzi3d.service.TextureLibraryService;
import kendzi.josm.kendzi3d.util.Direction;
import kendzi.josm.kendzi3d.util.DirectionParserUtil;
import kendzi.josm.kendzi3d.util.StringUtil;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;

import com.jogamp.opengl.util.awt.TextRenderer;

/**
 * Represent flat roof.
 *
 * @author Tomasz Kedziora (Kendzi)
 */
public class DormerRoof extends Roof {

    /** Log. */
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(DormerRoof.class);


    /** XXX
     * Font for axis.
     */
    private Font font = new Font("SansSerif", Font.BOLD, 24);

    /** XXX
     * For axis labels.
     */
    private TextRenderer axisLabelRenderer = new TextRenderer(this.font);

    /** XXX
     * For the axis labels.
     */
    private final static float SCALE_FACTOR = 0.01f;


    ModelRender modelRender;
//    private Model model;




    protected RoofDebugOut debug;


    private List<Point3d> biggerRec;


    /** Flat Roof.
     * @param pBuilding building
     * @param pFasadeTexture facade texture
     * @param pWay way
     * @param pPerspective perspective
     * @param pModelRender model render
     * @param pMetadataCacheService metadata cache service
     * @param pTextureLibraryService  texture library service
     */
    public DormerRoof(Building pBuilding, TextureData pFasadeTexture, Way pWay, Perspective3D pPerspective,
            ModelRender pModelRender, MetadataCacheService pMetadataCacheService, TextureLibraryService pTextureLibraryService) {
        super(pBuilding, pFasadeTexture, pWay, pPerspective, pModelRender, pMetadataCacheService,
                pTextureLibraryService);


        //
        //        list = new ArrayList<Point2D.Double>();
        //
        //        for (int i = 0; i < pWay.getNodesCount(); i++) {
        //            Node node = pWay.getNode(i);
        //
        //            double x = pPerspective.calcX(node.getEastNorth().getX());
        //            double y = pPerspective.calcY(node.getEastNorth().getY());
        //
        //            list.add(new Point2D.Double(x, y));
        //        }

        //        height = ModelUtil.getHeight(pWay, 1);
        this.height = pBuilding.getHeight();

        this.modelRender = pModelRender;
    }




    @Override
    public void buildModel() {


        DormerRoofModel roof = parseDormerRoof(
                /*this.points,*/ this.way, this.perspective);


        RoofTextureData rtd = new RoofTextureData();
        rtd.setFacadeTextrure(getFasadeTexture());
        rtd.setRoofTexture(getRoofTexture());


//        RoofOutput roofOutput = DormerRoofBuilder.build(roof, this.height, rtd);
//
//        this.debug = roofOutput.getDebug();
//
//        this.minHeight = this.height - roofOutput.getHeight();
//        this.model = roofOutput.getModel();

    }




    /**
     * @param points
     * @param perspective
     * @param way
     * @return
     */
    public static DormerRoofModel parseDormerRoof(
            /*List<Point2d> points,*/ OsmPrimitive way, Perspective3D perspective) {



        Map<String, String> keys = way.getKeys();

        String type = keys.get(OsmAttributeKeys._3DR_TYPE.getKey());
        if (StringUtil.isBlankOrNull(type)) {
            type = keys.get(OsmAttributeKeys.ROOF_SHAPE.getKey());
        }
        if (StringUtil.isBlankOrNull(type)) {
            type = keys.get(OsmAttributeKeys.BUILDING_ROOF_SHAPE.getKey());
        }

        String dormer = keys.get(OsmAttributeKeys._3DR_DORMERS.getKey());

        DormerRoofModel roof = new  DormerRoofModel();

//        roof.setBuilding(new PolygonList2d(points));
        RoofTypeAliasEnum roofType = Parser.parseRoofShape(type);
        if (roofType == null) {
            roofType = RoofTypeAliasEnum.FLAT;
        }
        roof.setRoofType(roofType);
        roof.setRoofTypeParameter(Parser.parseRoofTypeParameter(roofType, type));

        roof.setDormers(Parser.parseMultipleDormers(dormer));
        roof.setDormersFront(Parser.parseSiteDormers("front",keys));
        roof.setDormersLeft(Parser.parseSiteDormers("left",keys));
        roof.setDormersBack(Parser.parseSiteDormers("back",keys));
        roof.setDormersRight(Parser.parseSiteDormers("right",keys));

        roof.setMeasurements(Parser.parseMeasurements(keys));

        roof.setDirection(findDirection(way, perspective));
        roof.setOrientation(Parser.parseOrientation(keys));
        return roof;
    }

    private static Vector2d findDirection(OsmPrimitive pWay, Perspective3D pPerspective) {
        Vector2d ret = null;

        ret = findDirectionByRelation(pWay);
        if (ret != null) {
            return ret;
        }
        if (pWay instanceof Way) {
            ret = findDirectionByPoints((Way) pWay, pPerspective);
            if (ret != null) {
                return ret;
            }
        } else {
            //TODO
        }

        ret = findDirectionByDirectionTag(pWay);
        if (ret != null) {
            return ret;
        }

        return null;
    }


    private static Vector2d findDirectionByRelation(OsmPrimitive pWay) {
        // TODO
        // XXX add support for relations
        return null;
    }

    private static Vector2d findDirectionByPoints(Way pWay, Perspective3D pPerspective) {
        Point2d directionBegin = findPoint(OsmAttributeKeys._3DR_DIRECTION.getKey(), OsmAttributeValues.BEGIN.getValue(), pWay, pPerspective);
        if (directionBegin == null) {
            directionBegin = pPerspective.calcPoint(pWay.getNode(0));
        }



        Point2d directionEnd = findPoint(OsmAttributeKeys._3DR_DIRECTION.getKey(), OsmAttributeValues.END.getValue(), pWay, pPerspective);
        if (directionBegin != null && directionEnd != null) {
            Vector2d direction = new Vector2d(directionEnd);
            direction.sub(directionBegin);
            return direction;
        }

        return null;
    }

    static private Vector2d findDirectionByDirectionTag(OsmPrimitive pWay) {
        // XXX roof:direction
        String directionValue = OsmAttributeKeys.ROOF_DIRECTION.primitiveValue(pWay);
        if(StringUtil.isBlankOrNull(directionValue)) {
            directionValue = OsmAttributeKeys.DIRECTION.primitiveValue(pWay);
        }
//        directionValue = pWay.get("direction");
        Direction direction = DirectionParserUtil.parse(directionValue);
        if (direction != null) {
            return direction.getVector();
        }
        return null;
    }



    private static Point2d findPoint(String pKey, String pValue, Way pWay, Perspective3D pPerspective) {

        for (int i = 0; i < pWay.getNodesCount(); i++) {

            Node node = pWay.getNode(i);
            if (pValue.equals(node.get(pKey))) {
                return pPerspective.calcPoint(node);
            }
        }
        return null;
    }



    @Override
    public void draw(GL2 pGl, Camera pCamera) {
        this.modelRender.render(pGl, this.model);

        if (this.modelRender.isDebugging() && this.debug != null) {

            pGl.glDisable(GL2.GL_LIGHTING);

            // red
            pGl.glColor3f(1.0f, 0f, 0f);

            // Set line width to 4
            pGl.glLineWidth(6);
            // Repeat count, repeat pattern
            pGl.glLineStipple(1, (short) 0xf0f0);

            pGl.glBegin(GL2.GL_LINE_LOOP);

            List<Point3d> rectangle = biggerRect();

            for (int i = 0; i < rectangle.size(); i++) {

                Point3d point3d = rectangle.get(i);

                pGl.glVertex3d(point3d.x, point3d.y, point3d.z);


            }
            pGl.glEnd();

            for (int i = 0; i < rectangle.size(); i++) {
                Point3d point3d = rectangle.get(i);
                drawAxisText(pGl, ("rec point " + (i + 1)), point3d.x, point3d.y, point3d.z);
            }

            Point2d point2d = this.points.get(0);

            float[] rgba = new float[4];
            // green
            pGl.glColor3fv(Color.RED.darker().getRGBComponents(rgba), 0);

            double x = point2d.x;
            double y = this.minHeight;
            double z = -point2d.y;
            double d = 0.25;

//            pGl.glBegin(GL2.GL_LINE_LOOP);
//
//            pGl.glVertex3d(x + d, y, z);
//            pGl.glVertex3d(x, y, z - d);
//            pGl.glVertex3d(x - d, y, z);
//            pGl.glVertex3d(x, y, z + d);
//
//            pGl.glEnd();

            pGl.glPushMatrix();

            pGl.glTranslated(x, y, z);

            DrawUtil.drawDotY(pGl, d, 12);

            pGl.glPopMatrix();

        }
    }


    private List<Point3d> biggerRect() {
        if (this.biggerRec != null) {
            return this.biggerRec;

        }
        List<Point3d> ret = new ArrayList<Point3d>();
        double middleX = 0;
        double middleY = 0;
        double middleZ = 0;

        for (Point3d p :  this.debug.getRectangle()) {
            middleX = middleX + p.x;
            middleY = middleY + p.y;
            middleZ = middleZ + p.z;
        }

        Point3d middle = new Point3d(
                middleX / this.debug.getRectangle().size(),
                middleY / this.debug.getRectangle().size(),
                middleZ / this.debug.getRectangle().size());

        for (Point3d p :  this.debug.getRectangle()) {
            Vector3d v = new Vector3d(p);
            v.sub(middle);
            v.normalize();
            v.scale(0.1d);

            Point3d bigger= new Point3d(p);
            bigger.add(v);
            ret.add(bigger);
        }
        this.biggerRec = ret;
        return this.biggerRec;
    }




    /**
     * Draw txt at (x,y,z), with the text centered in the x-direction, facing
     * along the +z axis.
     *
     * @param gl
     * @param txt
     * @param x
     * @param y
     * @param z
     */
    private void drawAxisText(GL2 gl, String txt, double x, double y, double z) {

        Rectangle2D dim = this.axisLabelRenderer.getBounds(txt);
        float width = (float) dim.getWidth() * SCALE_FACTOR;

        this.axisLabelRenderer.begin3DRendering();
        this.axisLabelRenderer.draw3D(txt, (float) x - width / 2, (float)y, (float)z, SCALE_FACTOR);
        this.axisLabelRenderer.end3DRendering();
    }

    @Override
    public List<ExportItem> export(ExportModelConf conf) {
        if (this.model == null) {
            buildModel();
        }

        return Collections.singletonList(new ExportItem(this.model, new Point3d(this.getGlobalX(), 0, -this.getGlobalY()), new Vector3d(1,1,1)));
    }

}
