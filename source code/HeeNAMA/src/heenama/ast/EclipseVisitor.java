package heenama.ast;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class EclipseVisitor extends ASTVisitor {
	public ICompilationUnit unit = null;
	private int totalOfNonAPIMembers = 0;
	private int totalOfAPIMembers = 0;
	private int correctForNonAPI = 0;
	private int correctForAPI = 0;
	
	public void printMessage(){
		System.out.println("Non-API Members: " + totalOfNonAPIMembers);
		System.out.println("Correct of Non-API Members: " + correctForNonAPI);
		System.out.println("Precision of Non-API Members: " + correctForNonAPI * 1.0 / totalOfNonAPIMembers);
		System.out.println("API Members: " + totalOfAPIMembers);
		System.out.println("Correct of API Members: " + correctForAPI);
		System.out.println("Precision of API Members: " + correctForAPI * 1.0 / totalOfAPIMembers);
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
		System.out.println(node.toString());
		IVariableBinding fieldBinding = node.resolveFieldBinding();
		if(fieldBinding != null){
			ITypeBinding declaringClass = fieldBinding.getDeclaringClass();
			if(declaringClass != null){
				
				if(declaringClass.isFromSource())
					totalOfNonAPIMembers++;
//				else if(isAPI(declaringClass))
				else
					totalOfAPIMembers++;
				
//				String recommend = getJavaProposals(node.getName().getStartPosition());
//				System.out.println("EclipseRecommend:\t" + recommend);
//				
//				if(recommend.equals(fieldBinding.getName())){
//					if(declaringClass.isFromSource())
//						correctForNonAPI++;
//					else if(isAPI(declaringClass))
//						correctForAPI++;
//				}
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(QualifiedName node){
		System.out.println(node.toString());
		if(node.resolveBinding() instanceof IVariableBinding){
			IVariableBinding fieldBinding = (IVariableBinding)node.resolveBinding();
			if(fieldBinding != null){
				ITypeBinding declaringClass = fieldBinding.getDeclaringClass();
				if(declaringClass != null){
					
					if(declaringClass.isFromSource())
						totalOfNonAPIMembers++;
//					else if(isAPI(declaringClass))
					else
						totalOfAPIMembers++;
					
//					String recommend = getJavaProposals(node.getName().getStartPosition());
//					System.out.println("EclipseRecommend:\t" + recommend);
//					
//					if(recommend.equals(fieldBinding.getName())){
//						if(declaringClass.isFromSource())
//							correctForNonAPI++;
//						else if(isAPI(declaringClass))
//							correctForAPI++;
//					}
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(SuperFieldAccess node){
		System.out.println(node.toString());
		IVariableBinding fieldBinding = node.resolveFieldBinding();
		if(fieldBinding != null){
			ITypeBinding declaringClass = fieldBinding.getDeclaringClass();
			if(declaringClass != null){
				
				if(declaringClass.isFromSource())
					totalOfNonAPIMembers++;
//				else if(isAPI(declaringClass))
				else
					totalOfAPIMembers++;
				
//				String recommend = getJavaProposals(node.getName().getStartPosition());
//				System.out.println("EclipseRecommend:\t" + recommend);
//				
//				if(recommend.equals(fieldBinding.getName())){
//					if(declaringClass.isFromSource())
//						correctForNonAPI++;
//					else if(isAPI(declaringClass))
//						correctForAPI++;
//				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node){
		System.out.println(node.toString());
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if(methodBinding != null){
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			if(node.getExpression() != null && declaringClass != null){
				
				if(declaringClass.isFromSource())
					totalOfNonAPIMembers++;
//				else if(isAPI(declaringClass))
				else
					totalOfAPIMembers++;
				
//				String recommend = getJavaProposals(node.getName().getStartPosition());
//				System.out.println("EclipseRecommend:\t" + recommend);
//				
//				if(recommend.equals(methodBinding.getName())){
//					if(declaringClass.isFromSource())
//						correctForNonAPI++;
//					else if(isAPI(declaringClass))
//						correctForAPI++;
//				}
			}
		}
		return true;
	}
	
	@Override
	public boolean visit(SuperMethodInvocation node){
		System.out.println(node.toString());
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if(methodBinding != null){
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			if(declaringClass != null){
				
				if(declaringClass.isFromSource())
					totalOfNonAPIMembers++;
//				else if(isAPI(declaringClass))
				else
					totalOfAPIMembers++;
				
//				String recommend = getJavaProposals(node.getName().getStartPosition());
//				System.out.println("EclipseRecommend:\t" + recommend);
//				
//				if(recommend.equals(methodBinding.getName())){
//					if(declaringClass.isFromSource())
//						correctForNonAPI++;
//					else if(isAPI(declaringClass))
//						correctForAPI++;
//				}
			}
		}
		return true;
	}
	
	private String getJavaProposals(int position){
		CompletionProposalCollector collector = new CompletionProposalCollector(unit);
		try {
			unit.codeComplete(position, collector);
		} catch (JavaModelException e) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Code Completer Error", e.getMessage());
		}
		IJavaCompletionProposal[] jProps = collector.getJavaCompletionProposals();
		
		Arrays.sort(jProps, new Comparator<IJavaCompletionProposal>(){
			@Override
			public int compare(IJavaCompletionProposal one, IJavaCompletionProposal two){
				if(one.getRelevance() < two.getRelevance())
					return 1;
				else if(one.getRelevance() > two.getRelevance())
					return -1;
				else {
					return one.getDisplayString().compareTo(two.getDisplayString());
				}
			}
		});

		if(jProps.length == 0)
			return "";
		IJavaCompletionProposal proposal = jProps[0];
		String str = proposal.getDisplayString();
		str = str.substring(0, str.indexOf(' '));
		int index = str.indexOf('(');
		if(index > -1)
			str = str.substring(0, index);
		
		char[] chars = str.toCharArray();
		for(int i = 0; i < chars.length; i++)
			if(!Character.isJavaIdentifierPart(chars[i]))
				return str.substring(0, i);
		return str;
	}
	
}
