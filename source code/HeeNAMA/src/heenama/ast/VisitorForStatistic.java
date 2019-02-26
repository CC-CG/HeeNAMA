package heenama.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

public class VisitorForStatistic extends ASTVisitor {

	private int totalOfFieldAccess = 0;
	private int totalOfMethodInvocation = 0;
	private int totalOfQualifiedName = 0;
	private int totalOfSuperFieldAccess = 0;
	private int totalOfSuperMethodInvocation = 0;
	private int nullPointer = 0;
	
	public int printMessage(){
		int total = totalOfFieldAccess + totalOfMethodInvocation + totalOfQualifiedName + totalOfSuperFieldAccess + totalOfSuperMethodInvocation;
		return total;
	}

	@Override
	public boolean visit(PackageDeclaration node){
		return false;
	}
	
	@Override
	public boolean visit(ImportDeclaration node){
		return false;
	}
	
	@Override
	public boolean visit(AnnotationTypeDeclaration node){
		return false;
	}
	
	@Override
	public boolean visit(EnumDeclaration node){
		return false;
	}
	
	@Override
	public boolean visit(FieldAccess node){
		try{
			ITypeBinding typeBinding = node.resolveFieldBinding().getDeclaringClass();
			if(typeBinding != null && typeBinding.isFromSource())
				totalOfFieldAccess++;
		}catch(NullPointerException e){
			nullPointer++;
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node){
		try{
			ITypeBinding typeBinding = node.resolveMethodBinding().getDeclaringClass();
			if(node.getExpression() != null && typeBinding != null && typeBinding.isFromSource())
				totalOfMethodInvocation++;
		}catch(NullPointerException e){
			nullPointer++;
		}
		return true;
	}	
	
	@Override
	public boolean visit(QualifiedName node){
		try{
			if(node.resolveBinding() instanceof IVariableBinding){
				ITypeBinding typeBinding = ((IVariableBinding)node.resolveBinding()).getDeclaringClass();
				if(typeBinding != null && typeBinding.isFromSource())
					totalOfQualifiedName++;
			}
		}catch(NullPointerException e){
			nullPointer++;
		}
		return true;
	}
	
	@Override
	public boolean visit(SuperFieldAccess node){
		try{
			ITypeBinding typeBinding = node.resolveFieldBinding().getDeclaringClass();
			if(typeBinding != null && typeBinding.isFromSource())
				totalOfSuperFieldAccess++;
		}catch(NullPointerException e){
			nullPointer++;
		}
		return true;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node){
		try{
			ITypeBinding typeBinding = node.resolveMethodBinding().getDeclaringClass();
			if(typeBinding != null && typeBinding.isFromSource())
				totalOfSuperMethodInvocation++;
		}catch(NullPointerException e){
			nullPointer++;
		}
		return true;
	}
}
