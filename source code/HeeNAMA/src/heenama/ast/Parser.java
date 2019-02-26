package heenama.ast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class Parser {
	
	public static void startDetecting(){
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(fileChooser);
		String root;
		if(returnVal == JFileChooser.APPROVE_OPTION){
			root = fileChooser.getSelectedFile().getAbsolutePath();
//			System.out.println(root);
			
			File file = new File(root + "/features/");
			if(!file.exists())
				file.mkdir();
			
		}else{
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Path Error", "Choose a directory to save features.");
			return;
		}
		
		
		IProject proj = getProject();
		IJavaProject project = JavaCore.create(proj);
		
		Visitor visitor = new Visitor();
		VisitorForStatistic v = new VisitorForStatistic();
		visitor.setProjectName(project.getElementName());
		visitor.setRoot(root);
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());  
		try{
			dialog.run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try{
						IPackageFragment[] fragments = project.getPackageFragments();
						monitor.beginTask("Start Task", fragments.length);
						
						for(IPackageFragment fragment : fragments){
							if(monitor.isCanceled())
								break;
							
							if(fragment.getKind() != IPackageFragmentRoot.K_SOURCE)
								continue;
							
							ICompilationUnit[] compilationUnits = fragment.getCompilationUnits();
							if(compilationUnits.length == 0)
								continue;
							
							for(ICompilationUnit icu : compilationUnits){
								visitor.setUnit(icu);
								
								ASTParser astParser = ASTParser.newParser(AST.JLS8);
								astParser.setKind(ASTParser.K_COMPILATION_UNIT);
								astParser.setResolveBindings(true);
								astParser.setBindingsRecovery(true);
								astParser.setSource(icu);
								CompilationUnit unit = (CompilationUnit) (astParser.createAST(null));
								unit.accept(v);
								unit.accept(visitor);
							}
							monitor.worked(1);
						}
						monitor.done();
						
					}catch(JavaModelException e){
						e.printStackTrace();
					}
					
					
				}
			});
		}catch(InvocationTargetException | InterruptedException e){
			e.printStackTrace();
		}

//		int t = v.printMessage();
//		System.out.println("\ntotal = " + t);
//		int r = visitor.getResolved(); 
//		System.out.println("resolved = " + r + "\tproportion = " + r * 1.0 / t);
//		int s = visitor.getStacked();
//		System.out.println("stacked = " + s + "\tproportion = " + s * 1.0 / r);
//		visitor.printMessage();

		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Completed", "Completed!");
	}
	
	public static IProject getProject(){
		IProject project = null;  
		ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();    
		ISelection selection = selectionService.getSelection();    
		if(selection instanceof IStructuredSelection) {    
			Object element = ((IStructuredSelection)selection).getFirstElement();    
			
			if (element instanceof IResource) {    
				project= ((IResource)element).getProject();    
			} else if (element instanceof PackageFragmentRootContainer) {    
				IJavaProject jProject =     
						((PackageFragmentRootContainer)element).getJavaProject();    
				project = jProject.getProject();    
			} else if (element instanceof IJavaElement) {    
				IJavaProject jProject= ((IJavaElement)element).getJavaProject();    
				project = jProject.getProject();    
			}  
		}     
		return project;  
	}

}
