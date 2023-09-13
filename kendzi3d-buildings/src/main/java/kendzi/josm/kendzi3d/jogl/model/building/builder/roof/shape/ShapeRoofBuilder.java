/*
 * This software is provided "AS IS" without a warranty of any kind. You use it
 * on your own risk and responsibility!!! This file is shared under BSD v3
 * license. See readme.txt and BSD3 file for details.
 */

package kendzi.josm.kendzi3d.jogl.model.building.builder.roof.shape;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import kendzi.jogl.model.factory.MaterialFactory;
import kendzi.jogl.model.factory.MeshFactory;
import kendzi.jogl.model.factory.ModelFactory;
import kendzi.jogl.model.geometry.material.Material;
import kendzi.jogl.texture.dto.TextureData;
import kendzi.josm.kendzi3d.jogl.model.building.builder.roof.shape.registry.RoofTypeBuilderRegistry;
import kendzi.josm.kendzi3d.jogl.model.building.model.BuildingPart;
import kendzi.josm.kendzi3d.jogl.model.building.model.BuildingUtil;
import kendzi.josm.kendzi3d.jogl.model.building.model.roof.RoofModel;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.DormerTypeBuilder;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofDebugOut;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofMaterials;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofOutput;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofTextureIndex;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.RoofTypeOutput;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.dormer.RoofDormerTypeOutput;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.measurement.Measurement;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.measurement.MeasurementKey;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.measurement.MeasurementUnit;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.model.DormerRoofModel;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.model.RoofTextureData;
import kendzi.josm.kendzi3d.jogl.model.roof.mk.type.RoofTypeBuilder;
import kendzi.math.geometry.point.TransformationMatrix3d;
import kendzi.math.geometry.polygon.PolygonWithHolesList2d;

import org.ejml.simple.SimpleMatrix;

/**
 * Builder for solid roof with shapes described by name and world description.
 * It can support dormers.
 *
 * @author Tomasz Kędziora (kendzi)
 */
public class ShapeRoofBuilder {

    /**
     * Shape roof builder.
     *
     * @param buildingPart
     *
     * @param height
     *            roof maximal height. Taken from building
     * @param mf
     * @param roofTextureData
     *            texture data
     * @return roof model
     */
    public static RoofOutput build(BuildingPart buildingPart, double height, ModelFactory mf,
            RoofTextureData roofTextureData) {

        RoofMaterials roofMaterials = addMaterials(roofTextureData, mf);

        RoofModel roofModel = buildingPart.getRoof();

        validate(roofModel);

        DormerRoofModel dormerRoof = (DormerRoofModel) roofModel;

        {
            // FIXME
            if (dormerRoof.getMeasurements().get(MeasurementKey.HEIGHT_1) == null
                    && buildingPart.getRoofLevels() != null) {

                double roofHeight = buildingPart.getDefaultRoofHeight();
                if (buildingPart.getRoofLevels() < 1) {
                    roofHeight = 1d;
                }
                dormerRoof.getMeasurements().put(MeasurementKey.HEIGHT_1,
                        new Measurement(roofHeight, MeasurementUnit.METERS));
            }
        }

        PolygonWithHolesList2d buildingPolygon = BuildingUtil.buildingPartToPolygonWithHoles(buildingPart);

        List<Point2d> polygon = buildingPolygon.getOuter().getPoints();

        Point2d startPoint = polygon.get(0);

        RoofTypeBuilder roofType = getRoofType(dormerRoof);

        RoofTypeOutput rto = roofType.buildRoof(startPoint, buildingPolygon, dormerRoof, height, roofMaterials);

        List<RoofDormerTypeOutput> roofExtensionsList = DormerTypeBuilder.build(rto.getRoofHooksSpaces(), dormerRoof,
                dormerRoof.getMeasurements(), roofMaterials);

        double minHeight = height - rto.getHeight();

        buildModel(rto, roofExtensionsList, mf);

        RoofDebugOut debug = buildDebugInfo(rto, roofExtensionsList, startPoint, minHeight);

        RoofOutput out = new RoofOutput();
        out.setHeight(rto.getHeight());
        out.setDebug(debug);
        out.setHeightCalculator(rto.getHeightCalculator());
        return out;
    }

    private static void validate(RoofModel roofModel) {
        if (!(roofModel instanceof DormerRoofModel)) {
            throw new IllegalArgumentException("wrong roof model, should be DormerRoofModel but it is: " + roofModel);
        }
    }

    private static RoofTypeBuilder getRoofType(DormerRoofModel roof) {
        return RoofTypeBuilderRegistry.selectBuilder(roof.getRoofType());
    }

    private static RoofMaterials addMaterials(RoofTextureData pRoofTextureData, ModelFactory model) {
        RoofTextureIndex facadeTextureIndex = addMateraialTexture(pRoofTextureData.getFacadeTexture(),
                pRoofTextureData.getFacadeColor(), model);
        RoofTextureIndex roofTextureIndex = addMateraialTexture(pRoofTextureData.getRoofTexture(),
                pRoofTextureData.getRoofColor(), model);

        RoofMaterials roofMaterials = new RoofMaterials();
        roofMaterials.setFacade(facadeTextureIndex);
        roofMaterials.setRoof(roofTextureIndex);
        return roofMaterials;
    }

    /**
     * Add material representing texture to model.
     *
     * @param textureData
     * @param model
     * @return
     */
    private static RoofTextureIndex addMateraialTexture(TextureData textureData, Color color, ModelFactory model) {

        Material facadeMaterial = null;
        if (color != null) {
            facadeMaterial = MaterialFactory.createTextureColorMaterial(textureData.getTex0(), color);
        } else {
            facadeMaterial = MaterialFactory.createTextureMaterial(textureData.getTex0());
        }

        int facadeMaterialIndex = model.addMaterial(facadeMaterial);

        return new RoofTextureIndex(facadeMaterialIndex, textureData);
    }

    private static void transformMeshFactory(MeshFactory meshFactory, SimpleMatrix transformationMatrix) {

        MeshFactory mesh = meshFactory;

        SimpleMatrix normalMatrix = transformationMatrix.invert().transpose();

        Set<Vector3d> procesed = new HashSet<Vector3d>();

        List<Point3d> vertices = new ArrayList<Point3d>();
        for (int i = 0; i < mesh.vertices.size(); i++) {
            Point3d p = mesh.vertices.get(i);

            vertices.add(TransformationMatrix3d.transform(p, transformationMatrix));
        }
        mesh.vertices = vertices;

        List<Vector3d> normals = new ArrayList<Vector3d>();
        for (int i = 0; i < mesh.normals.size(); i++) {
            Vector3d v = mesh.normals.get(i);
            // if (procesed.contains(v)) {
            // continue;
            // }
            procesed.add(v);

            v = TransformationMatrix3d.transform(v, normalMatrix);
            // XXX !!!;
            v.normalize();

            normals.add(v);
        }
        mesh.normals = normals;
    }

    private static RoofDebugOut buildDebugInfo(RoofTypeOutput rto, List<RoofDormerTypeOutput> roofExtensionsList,
            Point2d startPoint, double height) {

        Point3d startPointMark = new Point3d(startPoint.x, height, -startPoint.y);

        List<Point3d> rectangleTransf = new ArrayList<Point3d>();

        List<Point3d> rectangle = rto.getRectangle();
        for (int i = 0; i < rectangle.size(); i++) {
            Point3d p = rectangle.get(i);

            rectangleTransf.add(TransformationMatrix3d.transform(p, rto.getTransformationMatrix()));
        }
        rto.setRectangle(rectangleTransf);
        RoofDebugOut out = new RoofDebugOut();
        out.setBbox(rectangleTransf);
        out.setStartPoint(startPointMark);
        return out;
    }

    private static void buildModel(RoofTypeOutput rto, List<RoofDormerTypeOutput> roofExtensionsList,
            ModelFactory modelFactory) {

        for (MeshFactory mf : rto.getMesh()) {
            transformMeshFactory(mf, rto.getTransformationMatrix());
            modelFactory.addMesh(mf);
        }

        for (RoofDormerTypeOutput roofDormerTypeOutput : roofExtensionsList) {
            if (roofDormerTypeOutput == null) {
                continue;
            }

            SimpleMatrix roofMatrix = rto.getTransformationMatrix()
                    .mult(roofDormerTypeOutput.getTransformationMatrix());

            for (MeshFactory mf : roofDormerTypeOutput.getMesh()) {

                transformMeshFactory(mf, roofMatrix);

                modelFactory.addMesh(mf);
            }
        }

    }
}
