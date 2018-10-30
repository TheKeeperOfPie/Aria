package com.winsonchiu.aria.framework.util

import android.graphics.Matrix
import android.graphics.RectF

/**
 * Copy of internal [androidx.transition.MatrixUtils]
 */
object MatrixUtils {

    val IDENTITY_MATRIX: Matrix = object : Matrix() {

        internal fun oops() {
            throw IllegalStateException("Matrix can not be modified")
        }

        override fun set(src: Matrix) {
            oops()
        }

        override fun reset() {
            oops()
        }

        override fun setTranslate(
                dx: Float,
                dy: Float
        ) {
            oops()
        }

        override fun setScale(
                sx: Float,
                sy: Float,
                px: Float,
                py: Float
        ) {
            oops()
        }

        override fun setScale(
                sx: Float,
                sy: Float
        ) {
            oops()
        }

        override fun setRotate(
                degrees: Float,
                px: Float,
                py: Float
        ) {
            oops()
        }

        override fun setRotate(degrees: Float) {
            oops()
        }

        override fun setSinCos(
                sinValue: Float,
                cosValue: Float,
                px: Float,
                py: Float
        ) {
            oops()
        }

        override fun setSinCos(
                sinValue: Float,
                cosValue: Float
        ) {
            oops()
        }

        override fun setSkew(
                kx: Float,
                ky: Float,
                px: Float,
                py: Float
        ) {
            oops()
        }

        override fun setSkew(
                kx: Float,
                ky: Float
        ) {
            oops()
        }

        override fun setConcat(
                a: Matrix,
                b: Matrix
        ): Boolean {
            oops()
            return false
        }

        override fun preTranslate(
                dx: Float,
                dy: Float
        ): Boolean {
            oops()
            return false
        }

        override fun preScale(
                sx: Float,
                sy: Float,
                px: Float,
                py: Float
        ): Boolean {
            oops()
            return false
        }

        override fun preScale(
                sx: Float,
                sy: Float
        ): Boolean {
            oops()
            return false
        }

        override fun preRotate(
                degrees: Float,
                px: Float,
                py: Float
        ): Boolean {
            oops()
            return false
        }

        override fun preRotate(degrees: Float): Boolean {
            oops()
            return false
        }

        override fun preSkew(
                kx: Float,
                ky: Float,
                px: Float,
                py: Float
        ): Boolean {
            oops()
            return false
        }

        override fun preSkew(
                kx: Float,
                ky: Float
        ): Boolean {
            oops()
            return false
        }

        override fun preConcat(other: Matrix): Boolean {
            oops()
            return false
        }

        override fun postTranslate(
                dx: Float,
                dy: Float
        ): Boolean {
            oops()
            return false
        }

        override fun postScale(
                sx: Float,
                sy: Float,
                px: Float,
                py: Float
        ): Boolean {
            oops()
            return false
        }

        override fun postScale(
                sx: Float,
                sy: Float
        ): Boolean {
            oops()
            return false
        }

        override fun postRotate(
                degrees: Float,
                px: Float,
                py: Float
        ): Boolean {
            oops()
            return false
        }

        override fun postRotate(degrees: Float): Boolean {
            oops()
            return false
        }

        override fun postSkew(
                kx: Float,
                ky: Float,
                px: Float,
                py: Float
        ): Boolean {
            oops()
            return false
        }

        override fun postSkew(
                kx: Float,
                ky: Float
        ): Boolean {
            oops()
            return false
        }

        override fun postConcat(other: Matrix): Boolean {
            oops()
            return false
        }

        override fun setRectToRect(
                src: RectF,
                dst: RectF,
                stf: Matrix.ScaleToFit
        ): Boolean {
            oops()
            return false
        }

        override fun setPolyToPoly(
                src: FloatArray,
                srcIndex: Int,
                dst: FloatArray,
                dstIndex: Int,
                pointCount: Int
        ): Boolean {
            oops()
            return false
        }

        override fun setValues(values: FloatArray) {
            oops()
        }

    }
}