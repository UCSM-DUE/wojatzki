package de.uni.due.ltl.interactiveStance.client;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.uni.due.ltl.interactiveStance.backend.BackEnd;
import de.uni.due.ltl.interactiveStance.backend.EvaluationResult;
import de.uni.due.ltl.interactiveStance.backend.ExperimentConfiguration;
import de.uni.due.ltl.interactiveStance.client.detectorViews.AdjustmentEvent;
import de.uni.due.ltl.interactiveStance.client.detectorViews.DetectorView_Expert;
import de.uni.due.ltl.interactiveStance.client.detectorViews.DetectorView_Expert_Adjustable;
import de.uni.due.ltl.interactiveStance.client.detectorViews.DetectorView_Simplified;
import de.uni.due.ltl.interactiveStance.experimentLogging.ExperimentLogging;
import de.uni.due.ltl.interactiveStance.experimentLogging.LogOutEvent;

import javax.servlet.annotation.WebServlet;
import java.util.HashSet;
import java.util.Set;

@Theme("valo-stance")
@Title("Interactive Stance Detector")
public class MainUI extends UI {

	//logging gets initialized in Config view
	private ExperimentLogging logging;
    private Navigator navigator;
    protected static final String LOGINVIEW = "login";
    protected static final String CONFIGVIEW = "config";
    protected static final String DETECTORVIEW = "detector";
    protected static final String ERRORVIEW = "error";
    protected static final String RESULTVIEW = "result";
    protected static final String ABLATIONVIEW = "ablation";

    private MenuBar menuBar = null;
    private VerticalLayout placeholder = new VerticalLayout();

    @Override
    protected void init(VaadinRequest request) {
        buildOutline();
        setUpNavigator();
    }

    private void setUpNavigator() {
    	 navigator = new Navigator(this, placeholder);
         navigator.addView(LOGINVIEW, new LoginView());
         navigator.setErrorView(new ErrorView());
         navigator.navigateTo(LOGINVIEW);
	}

	private void buildOutline() {
        menuBar = new MenuBar();
        MenuBar.MenuItem homepage = menuBar.addItem("log out", null, new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
            	System.out.println(logging);
            	new LogOutEvent(logging).persist(true);
                getUI().getNavigator().navigateTo(LOGINVIEW);
            }
        });

        VerticalLayout mainLayout = new VerticalLayout(menuBar, placeholder);
        placeholder.setMargin(false);
        setContent(mainLayout);
    }

    public void hideMenubar() {
        if (menuBar != null) {
            menuBar.setVisible(false);
        }
    }
    
    /**
     * go to configuration view and hands over the logging object
     * @param simpleMode
     */
    public void showConfigView(ExperimentLogging logging){
    	this.logging= logging;
    	navigator.addView(CONFIGVIEW, new ConfigView(logging));
        navigator.navigateTo(MainUI.CONFIGVIEW);
    }
    
    /**
     * go to result view and hands over a result object and the backend
     * @param simpleMode
     */
    public void showResult(EvaluationResult result, BackEnd service){
        navigator.addView(RESULTVIEW, new ResultView(result,service));
        navigator.navigateTo(MainUI.RESULTVIEW);
    }
    
    /**
     * go to detector and hands the flag that determines whether we are in simple mode or not
     * @param simpleMode
     * @param logging 
     */
    public void showDetectorView(ExperimentConfiguration config){
    	if(config.isSimpleMode()){
    		navigator.addView(DETECTORVIEW,new DetectorView_Simplified(logging,config));
    	}else{
    		if(config.getExperimentMode().equals("Fixed Threshold") || config.getExperimentMode().equals("Distributional Threshold")){
    			navigator.addView(DETECTORVIEW,new DetectorView_Expert_Adjustable(logging,config));
    		}else{
    			navigator.addView(DETECTORVIEW,new DetectorView_Expert(logging,config));
    		}
    	}
    	navigator.navigateTo(DETECTORVIEW);
    }
    
    /**
     * go to ablation view and hands over a result object and the backend
     * @param simpleMode
     */
    public void showAblationView(EvaluationResult result, BackEnd service) {
    	navigator.addView(ABLATIONVIEW, new AblationView(result,service));
        navigator.navigateTo(MainUI.ABLATIONVIEW);
	}
    
    public void showMenubar() {
        if (menuBar != null) {
            menuBar.setVisible(true);
        }
    }

    @WebServlet(urlPatterns = "/*", asyncSupported = true)
    @VaadinServletConfiguration(ui = MainUI.class, productionMode = false)
    public static class MainUIServlet extends VaadinServlet {

    }

	
}
