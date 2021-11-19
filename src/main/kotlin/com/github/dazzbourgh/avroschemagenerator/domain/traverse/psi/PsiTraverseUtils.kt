package com.github.dazzbourgh.avroschemagenerator.domain.traverse.psi

import com.github.dazzbourgh.avroschemagenerator.domain.traverse.PrimitiveType
import com.github.dazzbourgh.avroschemagenerator.domain.traverse.ResolveElementReference
import com.github.dazzbourgh.avroschemagenerator.domain.traverse.psi.PsiTraverse.PsiResolveElementReference
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.util.PsiUtil
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import java.util.*

typealias ClassName = String

object PsiTraverseUtils {
    internal inline fun <reified T : PsiElement> PsiElement.getAllDescendantsOfType(): List<T> {
        val queue: Queue<PsiElement> = LinkedList()
        val result = mutableListOf<T>()
        queue.add(this)
        while (!queue.isEmpty()) {
            val current = queue.remove()
            if (current is T) result.add(current)
            current.children.toList().forEach { queue.add(it) }
        }
        return result
    }

    internal inline fun <reified T : PsiElement> PsiElement.getFirstDescendantOfType(): T? =
        search(this) { it.children.toList() }

    internal inline fun <reified T : PsiElement> PsiElement.getFirstAscendantOfType(): T? =
        search(this) { listOf(it.parent) }

    private inline fun <reified T : S, reified S> search(element: S, getNextElements: (S) -> Iterable<S>): T? {
        val queue: Queue<S> = LinkedList()
        queue.add(element)
        while (!queue.isEmpty()) {
            val current = queue.remove()
            if (current is T) return current
            else getNextElements(current).forEach { queue.add(it) }
        }
        return null
    }

    internal inline fun <reified T : PsiElement> PsiElement.getLastDescendantOfType(): T? {
        val traversalQueue: Queue<PsiElement> = LinkedList()
        val childrenQueue: Queue<PsiElement> = LinkedList()
        traversalQueue.add(this)
        while (!traversalQueue.isEmpty()) {
            val current = traversalQueue.remove()
            current.children.forEach { traversalQueue.add(it) }
            childrenQueue.add(current)
        }
        return childrenQueue.reversed().firstOrNull { it is T } as T?
    }

    internal fun mapBoxedType(
        psiType: PsiClassReferenceType,
        primitiveTypeSupplier: (ClassName) -> PrimitiveType?
    ): PrimitiveType =
        primitiveTypeSupplier(psiType.className)
            ?: throw IllegalArgumentException("Only boxed primitive types and String are supported")

    internal fun isGeneric(psiTypeElement: PsiTypeElement): Boolean =
        (psiTypeElement
            .getChildOfType<PsiJavaCodeReferenceElement>()
            ?.getChildOfType<PsiReferenceParameterList>()
            ?.getChildrenOfType<PsiTypeElement>()
            ?.size ?: 0) > 0

    fun PsiElement.isCollection(resolveElementReference: ResolveElementReference<PsiElement> = PsiResolveElementReference): Boolean =
        when (this) {
            is PsiClass ->
                PsiUtil.getPackageName(this)?.contains("java.util") == true
                        && extends("Collection")
            is PsiTypeElement -> with(resolveElementReference) { resolveElementReference()?.isCollection() ?: false }
            else -> TODO()
        }

    fun PsiClass.extends(parentClassName: String): Boolean =
        extendsList?.referenceElements?.mapNotNull { it.referenceName }?.any { it == parentClassName } ?: false
}