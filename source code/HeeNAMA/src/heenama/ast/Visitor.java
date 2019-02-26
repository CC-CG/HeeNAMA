package heenama.ast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import heenama.util.IdentifierTokenizer;
import heenama.util.Levenshtein;

@SuppressWarnings({ "unchecked" })
public class Visitor extends ASTVisitor {

	private String projectName = null;
	public String root = null;
	public ICompilationUnit unit = null;
	
	private Stack<ITypeBinding> typeStack = new Stack<>(); 
	private Stack<String> nameStack = new Stack<>();
	private Stack<String> statementStack = new Stack<>();
	private Map<String, Map<String, Integer>> map = new TreeMap<String, Map<String, Integer>>();

	private int depthOfAccess = 0;
	private int stackedFieldAccess = 0;
	private int stackedMethodInvocation = 0;
	private int stackedQualifiedName = 0;
	private int stackedSuperFieldAccess = 0;
	private int stackedSuperMethodInvocation = 0;
	
	private int resolvedFieldAccess = 0;
	private int resolvedMethodInvocation = 0;
	private int resolvedQualifiedName = 0;
	private int resolvedSuperFieldAccess = 0;
	private int resolvedSuperMethodInvocation = 0;
	
	private int returnedByType = 0;
	private int returnedByExample = 0;
	private int rightExample = 0;
	private int returnedBySimilarity = 0;
	private int rightSimilarity = 0;
	private int returnedRandomly = 0;
	private int rightRandomly = 0;
	
	private int typeEqual = 0;
	private int correct = 0;
	private int eclipseCorrect = 0;
	private int slpCorrect = 0;
	
	public boolean useTypeFilter = true;
	private int candidate_num = 0;
	private int candidate_num_before_filter = 0;
	private int candidate_num_after_filter = 0;
	
	public void setUnit(ICompilationUnit icu){
		this.unit = icu;
	}
	
	public void setProjectName(String name){
		this.projectName = name;
	}
	
	public void setRoot(String root){
		this.root = root;
	}
	
	public int getResolved(){
		int resolved = resolvedFieldAccess + resolvedMethodInvocation + resolvedQualifiedName + resolvedSuperFieldAccess + resolvedSuperMethodInvocation;
		return resolved;
	}
	
	public int getStacked(){
		int stacked = stackedFieldAccess + stackedMethodInvocation + stackedQualifiedName + stackedSuperFieldAccess + stackedSuperMethodInvocation;
		return stacked;
	}

	public void printMessage(){
//		int resolved = resolvedFieldAccess + resolvedMethodInvocation + resolvedQualifiedName + resolvedSuperFieldAccess + resolvedSuperMethodInvocation;
		System.out.println("correct: " + correct);
//		System.out.println(" precision = " + correct * 1.0 / resolved);
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
	public boolean visit(TypeDeclaration node){
		if(node.resolveBinding() == null){
			hasBeenVisited = false;
			return false;
		}
//		System.out.println("TypeDelcaration:\t" + node.resolveBinding().getQualifiedName());
		typeStack.push(node.resolveBinding());
		hasBeenVisited = true;
		return true;
	}
	
	private boolean hasBeenVisited = true;
	
	@Override
	public void endVisit(TypeDeclaration node){
		if(hasBeenVisited)
			typeStack.pop();
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if(node.resolveBinding() == null)
			return true;
		statementStack.push("Parent:\t" + node.toString());
		nameStack.push(node.getName().toString());
		parseExpression(node.getInitializer(), node.resolveBinding().getType());
//		if(this.depthOfAccess > 0){
//			System.err.println(node);
//		}
		nameStack.pop();
		statementStack.pop();
		return false;
	}

	@Override
	public boolean visit(Assignment node) {
		if(node.resolveTypeBinding() == null)
			return true;
		statementStack.push("Parent:\t" + node.toString());
		nameStack.push(getLastIdentifier(node.getLeftHandSide()));
		parseExpression(node.getRightHandSide(), node.getLeftHandSide().resolveTypeBinding());
//		if(this.depthOfAccess > 0){
//			System.err.println(node);
//		}
		nameStack.pop();
		statementStack.pop();
		return false;
	}
	
	private void parseExpression(Expression expression, ITypeBinding typeBinding){
		if(expression == null || typeBinding == null)
			return;
		switch(expression.getNodeType()){
			case ASTNode.ARRAY_ACCESS:
				parseArrayAccess(expression, typeBinding);break;
			case ASTNode.ARRAY_INITIALIZER:
				parseArrayInitializer(expression, typeBinding);break;
			case ASTNode.ASSIGNMENT:
				parseAssignment(expression, typeBinding);break;
			case ASTNode.CLASS_INSTANCE_CREATION:
				parseClassInstanceCreation(expression, typeBinding);break;
			case ASTNode.CONDITIONAL_EXPRESSION:
				parseConditionalExpression(expression, typeBinding);break;
			case ASTNode.EXPRESSION_METHOD_REFERENCE:
				parseExpressionMethodReference(expression, typeBinding);break;
			case ASTNode.FIELD_ACCESS:
				parseFieldAccess(expression, typeBinding);break;
			case ASTNode.INFIX_EXPRESSION:
				parseInfixExpression(expression, typeBinding);break;
			case ASTNode.INSTANCEOF_EXPRESSION:
				parseInstanceofExpression(expression, typeBinding);break;
			case ASTNode.METHOD_INVOCATION:
				parseMethodInvocation(expression, typeBinding);break;
			case ASTNode.PARENTHESIZED_EXPRESSION:
				parseParenthesizedExpression(expression, typeBinding);break;
			case ASTNode.POSTFIX_EXPRESSION:
				parsePostfixExpression(expression, typeBinding);break;
			case ASTNode.PREFIX_EXPRESSION:
				parsePrefixExpression(expression, typeBinding);break;
			case ASTNode.QUALIFIED_NAME:
				parseQualifiedName(expression, typeBinding);break;
			case ASTNode.SUPER_FIELD_ACCESS:
				parseSuperFieldAccess(expression, typeBinding);break;
			case ASTNode.SUPER_METHOD_INVOCATION:
				parseSuperMethodInvocation(expression, typeBinding);break;
			case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
				parseVariableDeclarationExpression(expression, typeBinding);break;
		}
	}

	private void parseArrayAccess(Expression expression, ITypeBinding typeBinding) {
		ArrayAccess arrayAccess = (ArrayAccess)expression;
		Expression array = arrayAccess.getArray();
		parseExpression(array, typeBinding);
	}

	private void parseArrayInitializer(Expression expression, ITypeBinding typeBinding) {
		ArrayInitializer arrayInitializer = (ArrayInitializer)expression;
		List<Expression> expressions = arrayInitializer.expressions();
		for(Expression exp : expressions){
			parseExpression(exp, typeBinding.getElementType());
		}
	}
	
	private void parseAssignment(Expression expression, ITypeBinding typeBinding) {
		Assignment assignment = (Assignment)expression;
		Expression leftHandSide = assignment.getLeftHandSide();
		parseExpression(leftHandSide, typeBinding);
		
		statementStack.push("Parent:\t" + assignment.toString());
		nameStack.push(getLastIdentifier(leftHandSide));
		parseExpression(assignment.getRightHandSide(), leftHandSide.resolveTypeBinding());
		nameStack.pop();
		statementStack.pop();
	}

	private void parseClassInstanceCreation(Expression expression, ITypeBinding typeBinding) {
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation)expression;
		parseExpression(classInstanceCreation.getExpression(), typeBinding);
	}

	private void parseConditionalExpression(Expression expression, ITypeBinding typeBinding) {
		ConditionalExpression conditionalExpression = (ConditionalExpression)expression;
		parseExpression(conditionalExpression.getExpression(), typeBinding);
		parseExpression(conditionalExpression.getThenExpression(), typeBinding);
		parseExpression(conditionalExpression.getElseExpression(), typeBinding);
	}
	
	private void parseExpressionMethodReference(Expression expression, ITypeBinding typeBinding){
		ExpressionMethodReference expressionMethodReference = (ExpressionMethodReference)expression;
		parseExpression(expressionMethodReference.getExpression(), typeBinding);
	}

	private void parseInfixExpression(Expression expression, ITypeBinding typeBinding) {
		InfixExpression infixExpression = (InfixExpression)expression;
		parseExpression(infixExpression.getLeftOperand(), typeBinding);
		parseExpression(infixExpression.getRightOperand(), typeBinding);
		List<Expression> extendedOperands = infixExpression.extendedOperands();
		for(Expression exp : extendedOperands){
			parseExpression(exp, typeBinding);
		}
	}

	private void parseInstanceofExpression(Expression expression, ITypeBinding typeBinding) {
		InstanceofExpression instanceofExpression = (InstanceofExpression)expression;
		parseExpression(instanceofExpression.getLeftOperand(), typeBinding);
	}

	private void parseParenthesizedExpression(Expression expression, ITypeBinding typeBinding) {
		ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression)expression;
		parseExpression(parenthesizedExpression.getExpression(), typeBinding);
	}

	private void parsePostfixExpression(Expression expression, ITypeBinding typeBinding) {
		PostfixExpression postfixExpression = (PostfixExpression)expression;
		parseExpression(postfixExpression.getOperand(), typeBinding);
	}

	private void parsePrefixExpression(Expression expression, ITypeBinding typeBinding) {
		PrefixExpression prefixExpression = (PrefixExpression)expression;
		parseExpression(prefixExpression.getOperand(), typeBinding);
	}
	
	private void parseVariableDeclarationExpression(Expression expression, ITypeBinding typeBinding){
		VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
		List<VariableDeclarationFragment> fragments = variableDeclarationExpression.fragments();
		for(VariableDeclarationFragment fragment : fragments){
			statementStack.push("Parent:\t" + fragment.toString());
			nameStack.push(fragment.getName().toString());
			parseExpression(fragment.getInitializer(), fragment.resolveBinding().getType());
			nameStack.pop();
			statementStack.pop();
		}
	}
	
	private void parseFieldAccess(Expression expression, ITypeBinding typeBinding) {
		FieldAccess fieldAccess = (FieldAccess)expression;
		this.depthOfAccess++;
		parseExpression(fieldAccess.getExpression(), typeBinding);
		this.depthOfAccess--;
		
		IVariableBinding fieldBinding = fieldAccess.resolveFieldBinding();
		if(fieldBinding != null){
			ITypeBinding declaringClass = fieldBinding.getDeclaringClass();
			if(declaringClass != null && declaringClass.isFromSource()){
				List<String> results = new ArrayList<String>();
//				System.out.println(statementStack.peek());
				results.add(statementStack.peek());
//				System.out.println("FieldAccess:\t" + fieldAccess + "\t==>>\t" + typeBinding.getName() + ": " + nameStack.peek());
				results.add("FieldAccess");
				results.add(fieldAccess.toString());
				results.add(typeBinding.getName() + ": " + nameStack.peek());
				
				if(fieldBinding.getType().isEqualTo(typeBinding) || fieldBinding.getType().isSubTypeCompatible(typeBinding)){
					typeEqual++;
				}

				String recommend = "";
				String objectName = getLastIdentifier(fieldAccess.getExpression());
				recommend = getCandidateWithCache(typeBinding, nameStack.peek() + "=" + objectName, fieldBinding.getName());
				int rule_id = 0;
				String similarity = "1.0";
				if(recommend.length() > 0){
					returnedByExample++;
					rule_id = 1;
					if(recommend.equals(fieldBinding.getName()))
						rightExample++;
				}else{
					
					
					candidate_num = 0;
					List<IBinding> candidates = getCandidates(declaringClass, typeStack.peek(), typeBinding, fieldBinding);
					
					String[] strs = getMostSimilarCandidateWithoutExpanding(candidates, nameStack.peek());
					recommend = strs[0];
					similarity = strs[1];
//					System.err.println(similarity);
					if(recommend.length() > 0){
						returnedBySimilarity++;
						rule_id = 2;
						if(recommend.equals(fieldBinding.getName()))
							rightSimilarity++;
					}else{
						recommend = getRandomCandidate(candidates);
						returnedRandomly ++;
						rule_id = 3;
						if(recommend.equals(fieldBinding.getName()))
							rightRandomly++;
					}
				}
				
				if(this.depthOfAccess > 0){
					this.stackedFieldAccess++;
				}
				
				boolean flag = recommend.equals(fieldBinding.getName()) ? true : false;
				writeCSV(typeBinding, nameStack.peek(), objectName, recommend, flag, rule_id, similarity, candidate_num);
//				System.out.println("MyRecommend:\t" + recommend);
				results.add(recommend);
				if(flag){
					correct++;
					results.add("1");
				}else{
					results.add("0");
				}
				
				resolvedFieldAccess++;
//				System.out.println();
				writeResults(results);
			}
		}
	}

	private void parseQualifiedName(Expression expression, ITypeBinding typeBinding){
		QualifiedName qualifiedName = (QualifiedName)expression;
		this.depthOfAccess++;
		parseExpression((Expression)qualifiedName.getQualifier(), typeBinding);
		this.depthOfAccess--;
		
		if(qualifiedName.resolveBinding() instanceof IVariableBinding){
			IVariableBinding fieldBinding = (IVariableBinding)qualifiedName.resolveBinding();
			if(fieldBinding != null){
				ITypeBinding declaringClass = fieldBinding.getDeclaringClass();
				if(declaringClass != null && declaringClass.isFromSource()){
					List<String> results = new ArrayList<String>();
//					System.out.println(statementStack.peek());
					results.add(statementStack.peek());
//					System.out.println("QualifiedName:\t" + qualifiedName + "\t==>>\t" + typeBinding.getName() + ": " + nameStack.peek());
					results.add("QualifiedName");
					results.add(qualifiedName.toString());
					results.add(typeBinding.getName() + ": " + nameStack.peek());
					
					if(fieldBinding.getType().isEqualTo(typeBinding) || fieldBinding.getType().isSubTypeCompatible(typeBinding)){
						typeEqual++;
					}
					
					String recommend = "";
					String objectName = getLastIdentifier(qualifiedName.getQualifier());
					recommend = getCandidateWithCache(typeBinding, nameStack.peek() + "=" + objectName, fieldBinding.getName());
					int rule_id = 0;
					String similarity = "1.0";
					if(recommend.length() > 0){
						returnedByExample++;
						rule_id = 1;
						if(recommend.equals(fieldBinding.getName()))
							rightExample++;
					}else{
						
						candidate_num = 0;
						List<IBinding> candidates = getCandidates(declaringClass, typeStack.peek(), typeBinding, fieldBinding);
						
						String[] strs = getMostSimilarCandidateWithoutExpanding(candidates, nameStack.peek());
						recommend = strs[0];
						similarity = strs[1];
//						System.err.println(similarity);
						if(recommend.length() > 0){
							returnedBySimilarity++;
							rule_id = 2;
							if(recommend.equals(fieldBinding.getName()))
								rightSimilarity++;
						}else{
							recommend = getRandomCandidate(candidates);
							returnedRandomly++;
							rule_id = 3;
							if(recommend.equals(fieldBinding.getName()))
								rightRandomly++;
						}
					}
					
					if(this.depthOfAccess > 0){
						this.stackedQualifiedName++;
					}

					boolean flag = recommend.equals(fieldBinding.getName()) ? true : false;
					writeCSV(typeBinding, nameStack.peek(), objectName, recommend, flag, rule_id, similarity, candidate_num);
//					System.out.println("MyRecommend:\t" + recommend);
					results.add(recommend);
					if(flag){
						correct++;
						results.add("1");
					}else{
						results.add("0");
					}
					
					resolvedQualifiedName++;
//					System.out.println();
					writeResults(results);
				}
			}
		}
	}

	private void parseSuperFieldAccess(Expression expression, ITypeBinding typeBinding) {
		SuperFieldAccess superFieldAccess = (SuperFieldAccess)expression;
		IVariableBinding fieldBinding = superFieldAccess.resolveFieldBinding();
		if(fieldBinding != null){
			ITypeBinding declaringClass = fieldBinding.getDeclaringClass();
			if(declaringClass != null && declaringClass.isFromSource()){
				List<String> results = new ArrayList<String>();
//				System.out.println(statementStack.peek());
				results.add(statementStack.peek());
//				System.out.println("SuperFieldAccess:\t" + superFieldAccess + "\t==>>\t" + typeBinding.getName() + ": " + nameStack.peek());
				results.add("SuperFieldAccess");
				results.add(superFieldAccess.toString());
				results.add(typeBinding.getName() + ": " + nameStack.peek());
				
				
				if(fieldBinding.getType().isEqualTo(typeBinding) || fieldBinding.getType().isSubTypeCompatible(typeBinding)){
					typeEqual++;
				}
				
				String recommend = "";
				String objectName = "super";
				recommend = getCandidateWithCache(typeBinding, nameStack.peek() + "=super", fieldBinding.getName());
				int rule_id = 0;
				String similarity = "1.0";
				if(recommend.length() > 0){
					returnedByExample++;
					rule_id = 1;
					if(recommend.equals(fieldBinding.getName()))
						rightExample++;
				}else{
					candidate_num = 0;
					List<IBinding> candidates = getCandidates(declaringClass, typeStack.peek(), typeBinding, fieldBinding);
					
					String[] strs = getMostSimilarCandidateWithoutExpanding(candidates, nameStack.peek());
					recommend = strs[0];
					similarity = strs[1];
//					System.err.println(similarity);
					if(recommend.length() > 0){
						returnedBySimilarity++;
						rule_id = 2;
						if(recommend.equals(fieldBinding.getName()))
							rightSimilarity++;
					}else{
						recommend = getRandomCandidate(candidates);
						returnedRandomly++;
						rule_id = 3;
						if(recommend.equals(fieldBinding.getName()))
							rightRandomly++;
					}
				}
				
				if(this.depthOfAccess > 0){
					this.stackedSuperFieldAccess++;
				}

				boolean flag = recommend.equals(fieldBinding.getName()) ? true : false;
				writeCSV(typeBinding, nameStack.peek(), objectName, recommend, flag, rule_id, similarity, candidate_num);
//				System.out.println("MyRecommend:\t" + recommend);
				results.add(recommend);
				if(flag){
					correct++;
					results.add("1");
				}else{
					results.add("0");
				}
				
				resolvedSuperFieldAccess++;
//				System.out.println();
				writeResults(results);
			}
		}
	}

	private void parseMethodInvocation(Expression expression, ITypeBinding typeBinding) {
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		
		if(methodInvocation.getExpression() != null){
			this.depthOfAccess++;
			parseExpression(methodInvocation.getExpression(), typeBinding);
			this.depthOfAccess--;
			
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if(methodBinding != null){
				ITypeBinding declaringClass = methodBinding.getDeclaringClass();
				if(declaringClass != null && declaringClass.isFromSource()){
					List<String> results = new ArrayList<String>();
//					System.out.println(statementStack.peek());
					results.add(statementStack.peek());
//					System.out.println("MethodInvocation:\t" + methodInvocation + "\t==>>\t" + typeBinding.getName() + ": " + nameStack.peek());
					results.add("MethodInvocation");
					results.add(methodInvocation.toString());
					results.add(typeBinding.getName() + ": " + nameStack.peek());
					
					if(methodBinding.getReturnType().isEqualTo(typeBinding) || methodBinding.getReturnType().isSubTypeCompatible(typeBinding)){
						typeEqual++;
					}
					
					String recommend = "";
					String objectName = getLastIdentifier(methodInvocation.getExpression());
					recommend = getCandidateWithCache(typeBinding, nameStack.peek() + "=" + objectName, methodBinding.getName());
					int rule_id = 0;
					String similarity = "1.0";
					if(recommend.length() > 0){
						returnedByExample++;
						rule_id = 1;
						if(recommend.equals(methodBinding.getName()))
							rightExample++;
					}else{
						candidate_num = 0;
						List<IBinding> candidates = getCandidates(declaringClass, typeStack.peek(), typeBinding, methodBinding);
						
						String[] strs = getMostSimilarCandidateWithoutExpanding(candidates, nameStack.peek());
						recommend = strs[0];
						similarity = strs[1];
//						System.err.println(similarity);
						if(recommend.length() > 0){
							returnedBySimilarity++;
							rule_id = 2;
							if(recommend.equals(methodBinding.getName()))
								rightSimilarity++;
						}else{
							recommend = getRandomCandidate(candidates);
							returnedRandomly++;
							rule_id = 3;
							if(recommend.equals(methodBinding.getName()))
								rightRandomly++;
						}
					}
					
					if(this.depthOfAccess > 0){
						this.stackedMethodInvocation++;
					}

					boolean flag = recommend.equals(methodBinding.getName()) ? true : false;
					writeCSV(typeBinding, nameStack.peek(), objectName, recommend, flag, rule_id, similarity, candidate_num);
//					System.out.println("MyRecommend:\t" + recommend);
					results.add(recommend);
					if(flag){
						correct++;
						results.add("1");
					}else{
						results.add("0");
					}
					
					resolvedMethodInvocation++;
//					System.out.println();
					writeResults(results);
				}
			}
		}
		
	}

	private void parseSuperMethodInvocation(Expression expression, ITypeBinding typeBinding) {
		SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)expression;
		IMethodBinding methodBinding = superMethodInvocation.resolveMethodBinding();
		if(methodBinding != null){
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			if(declaringClass != null && declaringClass.isFromSource()){
				List<String> results = new ArrayList<String>();
//				System.out.println(statementStack.peek());
				results.add(statementStack.peek());
//				System.out.println("SuperMethodInvocation:\t" + superMethodInvocation + "\t==>>\t" + typeBinding.getName() + ": " + nameStack.peek());
				results.add("SuperMethodInvocation");
				results.add(superMethodInvocation.toString());
				results.add(typeBinding.getName() + ": " + nameStack.peek());
				
				if(methodBinding.getReturnType().isEqualTo(typeBinding) || methodBinding.getReturnType().isSubTypeCompatible(typeBinding)){
					typeEqual++;
				}
				
				String recommend = "";
				String objectName = "super";
				recommend = getCandidateWithCache(typeBinding, nameStack.peek() + "=super", methodBinding.getName());
				int rule_id = 0;
				String similarity = "1.0";
				if(recommend.length() > 0){
					returnedByExample++;
					rule_id = 1;
					if(recommend.equals(methodBinding.getName()))
						rightExample++;
				}else{
					candidate_num = 0;
					List<IBinding> candidates = getCandidates(declaringClass, typeStack.peek(), typeBinding, methodBinding);
					
					String[] strs = getMostSimilarCandidateWithoutExpanding(candidates, nameStack.peek());
					recommend = strs[0];
					similarity = strs[1];
//					System.err.println(similarity);
					if(recommend.length() > 0){
						returnedBySimilarity++;
						rule_id = 2;
						if(recommend.equals(methodBinding.getName()))
							rightSimilarity++;
					}else{
						recommend = getRandomCandidate(candidates);
						returnedRandomly++;
						rule_id = 3;
						if(recommend.equals(methodBinding.getName()))
							rightRandomly++;
					}
				}
				
				if(this.depthOfAccess > 0){
					this.stackedSuperMethodInvocation++;
				}

				boolean flag = recommend.equals(methodBinding.getName()) ? true : false;
				writeCSV(typeBinding, nameStack.peek(), objectName, recommend, flag, rule_id, similarity, candidate_num);
//				System.out.println("MyRecommend:\t" + recommend);
				results.add(recommend);
				if(flag){
					correct++;
					results.add("1");
				}else{
					results.add("0");
				}
				
				resolvedSuperMethodInvocation++;
//				System.out.println();
				writeResults(results);
			}
			
		}
	}
	
	private void writeCSV(ITypeBinding type, String name, String object, String recommend, boolean label, int rule_id, String similarity, int candidate_num){
		BufferedWriter writer = null;
		if(rule_id == 0)
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "", "rule_id = 0");
		try {
			writer = new BufferedWriter(new FileWriter(root + "/features/" + projectName +  "_features.csv", true));
			if(label)
				writer.write("1," + rule_id + "," + similarity + "," + candidate_num + "," + type.getErasure().getName() + ", ," + IdentifierTokenizer.tokenize(name).replace(' ', ',') + ",=," + IdentifierTokenizer.tokenize(object).replace(' ', ',') + ",.," + IdentifierTokenizer.tokenize(recommend).replace(' ', ','));
			else
				writer.write("0," + rule_id + "," + similarity + "," + candidate_num + "," + type.getErasure().getName() + ", ," + IdentifierTokenizer.tokenize(name).replace(' ', ',') + ",=," + IdentifierTokenizer.tokenize(object).replace(' ', ',') + ",.," + IdentifierTokenizer.tokenize(recommend).replace(' ', ','));
			writer.newLine();
			writer.flush();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(writer != null)
				try{
					writer.close();
				}catch(IOException e){
					e.printStackTrace();
				}
		}
	}
	
	private void writeResults(List<String> results){
		String statement = results.get(0).replaceAll(",", "'").replaceAll("\n", " ").trim().replaceFirst("Parent:", "");
		String node = results.get(2).replaceAll(",", "'").replaceAll("\n", " ").trim();
		String myrecommend = results.get(4).replaceAll(",", "'").replaceAll("\n", " ").trim();
		String myflag = results.get(5).trim();
		String path = "";
		try {
			path = unit.getCorrespondingResource().getLocation().toOSString().replace(",", "'").replaceAll("\n", " ").trim();
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
		
		File file = new File(root + "/features/" + this.projectName +  "_results.csv");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file, true));
			if(!file.exists()){
				writer.write("Member access, Assignment, HeeNAMA, T/F, Path");
				writer.newLine();
			}
			writer.write(node + "," + statement + "," + myrecommend + "," + myflag + "," + path);
			writer.newLine();
			writer.flush();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			if(writer != null)
				try{
					writer.close();
				}catch(IOException e){
					e.printStackTrace();
				}
		}
	}
	
	private String getCandidateWithCache(ITypeBinding typeBinding, String identifiers, String name){
		String recommend = "";
		String line = typeBinding.getName() + " " + identifiers;
		if(map.containsKey(line)){
			Map<String, Integer> map1 = map.get(line);
			int frequency = 0;
			for(String str : map1.keySet()){
				int temp = map1.get(str).intValue();
				if(temp >= frequency){
					frequency = temp;
					recommend = str;
				}
			}
			if(map1.containsKey(name)){
				int temp = map1.get(name).intValue();
				map1.put(name, temp + 1);
				map.put(line, map1);
			}else{
				map1.put(name, Integer.valueOf(1));
				map.put(line, map1);
			}
		}else{
			Map<String, Integer> map1 = new TreeMap<String, Integer>();
			map1.put(name, Integer.valueOf(1));
			map.put(line, map1);
		}
		return recommend;
	}
	
	private String[] getMostSimilarCandidateWithoutExpanding(List<IBinding> candidates, String name){
		IBinding mostSimilar = null;
		float highestSimilarity = 0.0f;
		for(IBinding binding : candidates){
			float similarity = Levenshtein.getSimilarityRatio(binding.getName(), name);
			if(similarity > highestSimilarity){
				highestSimilarity = similarity;
				mostSimilar = binding;
			}
		}
		highestSimilarity = (float)(Math.round(highestSimilarity * 100)) / 100;
		if(mostSimilar == null)
			return new String[]{"", "1.0"};
		return new String[]{mostSimilar.getName(), String.valueOf(highestSimilarity)};
	}
	
	private String getRandomCandidate(List<IBinding> candidates){
		if(candidates.size() > 0){
			Random random = new Random();
			return candidates.get(random.nextInt(candidates.size())).getName();
		}else{
//			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "RandomCandidate", "No random candidates to be returned!");
			return "";
		}
	}
	
	private List<IBinding> getCandidates(ITypeBinding declaringClass, ITypeBinding editingClass, ITypeBinding returnType, IBinding target){
		if(declaringClass == null || editingClass == null || returnType == null || target == null)
			return null;
		List<IVariableBinding> fields = new ArrayList<IVariableBinding>();
		List<IMethodBinding> methods = new ArrayList<IMethodBinding>();
		while(declaringClass != null){
			IVariableBinding[] vs = declaringClass.getDeclaredFields();
			IMethodBinding[] ms = declaringClass.getDeclaredMethods();
			if(editingClass.isEqualTo(declaringClass) || (editingClass.isNested() && editingClass.getDeclaringClass().isEqualTo(declaringClass)) 
					|| (declaringClass.isNested() && declaringClass.getDeclaringClass().isEqualTo(editingClass))){
				for(IVariableBinding v : vs){
						fields.add(v);
				}
				for(IMethodBinding m : ms){
					if(m.isConstructor())
						continue;
					methods.add(m);
				}
			}else if(editingClass.getPackage().isEqualTo(declaringClass.getPackage())){
				for(IVariableBinding v : vs){
					if(!Modifier.isPrivate(v.getModifiers()))
							fields.add(v);
				}
				for(IMethodBinding m : ms){
					if(m.isConstructor())
						continue;
					if(!Modifier.isPrivate(m.getModifiers()))
						methods.add(m);
				}
			}else if(editingClass.isSubTypeCompatible(declaringClass)){
				for(IVariableBinding v : vs){
					if(Modifier.isPublic(v.getModifiers()) || Modifier.isProtected(v.getModifiers()))
							fields.add(v);
				}
				for(IMethodBinding m : ms){
					if(m.isConstructor())
						continue;
					if(Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()))
						methods.add(m);
				}
			}else{
				for(IVariableBinding v : vs){
					if(Modifier.isPublic(v.getModifiers()))
							fields.add(v);
				}
				for(IMethodBinding m : ms){
					if(m.isConstructor())
						continue;
					if(Modifier.isPublic(m.getModifiers()))
						methods.add(m);
				}
			}
			declaringClass = declaringClass.getSuperclass();
		}
		
		candidate_num = fields.size() + methods.size();
		candidate_num_before_filter = candidate_num_before_filter + candidate_num;
		
		List<IBinding> candidates = new ArrayList<IBinding>();
		if(useTypeFilter){
			for(IVariableBinding v : fields){
				if(v.getType().isEqualTo(returnType) || v.getType().isSubTypeCompatible(returnType))
					candidates.add(v);
			}
			for(IMethodBinding m : methods){
				if(m.getReturnType().isEqualTo(returnType) || m.getReturnType().isSubTypeCompatible(returnType))
					candidates.add(m);
			}
		}else{
			for(IVariableBinding v : fields)
				candidates.add(v);
			for(IMethodBinding m : methods)
				candidates.add(m);
		}
		for(IBinding binding : candidates){
			if(binding.getName().equals(target.getName())){
				returnedByType++;
				break;
			}
		}
		candidate_num_after_filter = candidate_num_after_filter + candidates.size();
		return candidates;
	}

	private String getLastIdentifier(Expression expression){
		if(expression == null)
			return "";
		String name = "";
		switch(expression.getNodeType()){
			case ASTNode.FIELD_ACCESS:
				name = ((FieldAccess)expression).getName().getIdentifier();
				break;
			case ASTNode.SIMPLE_NAME:
				name = ((SimpleName)expression).getIdentifier();
				break;
			case ASTNode.QUALIFIED_NAME:
				name = ((QualifiedName)expression).getName().getIdentifier();
				break;
			case ASTNode.ARRAY_ACCESS:
				name = getLastIdentifier(((ArrayAccess)expression).getArray());
				break;
			case ASTNode.METHOD_INVOCATION:
				name = ((MethodInvocation)expression).getName().getIdentifier();
				break;
			case ASTNode.THIS_EXPRESSION:
				name = "this";
				break;
			case ASTNode.SUPER_FIELD_ACCESS:
				name = ((SuperFieldAccess)expression).getName().getIdentifier();
				break;
			case ASTNode.CLASS_INSTANCE_CREATION:
				name = ((ClassInstanceCreation)expression).getType().resolveBinding().getName();
				break;
			case ASTNode.PARENTHESIZED_EXPRESSION:
				name = getLastIdentifier(((ParenthesizedExpression)expression).getExpression());
				break;
			case ASTNode.CAST_EXPRESSION:
				name = getLastIdentifier(((CastExpression)expression).getExpression());
				break;
			case ASTNode.ASSIGNMENT:
				name = getLastIdentifier(((Assignment)expression).getRightHandSide());
				break;
			default:
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "", expression.getNodeType() + "");
		}
		return name;
	}
}
