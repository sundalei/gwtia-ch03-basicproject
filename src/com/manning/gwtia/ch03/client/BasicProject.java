package com.manning.gwtia.ch03.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

@SuppressWarnings("deprecation")
public class BasicProject implements EntryPoint, ValueChangeHandler<String> {

	static final int DECK_HOME = 0;
	static final int DECK_PRODUCTS = 1;
	static final int DECK_CONTACT = 2;

	static final String TOKEN_HOME = "Home";
	static final String TOKEN_PRODUCTS = "Products";
	static final String TOKEN_CONTACT = "Contact";

	private static final String LOGO_IMAGE_NAME = "gwtia.png";

	PopupPanel searchRequest;

	enum Pages {
		HOME(DECK_HOME, TOKEN_HOME), PRODUCTS(DECK_PRODUCTS, TOKEN_PRODUCTS), CONTACT(DECK_CONTACT, TOKEN_CONTACT);

		private int val;
		private String text;

		int getVal() {
			return val;
		}

		String getText() {
			return text;
		}

		Pages(int val, String text) {
			this.val = val;
			this.text = text;
		};
	}

	/**
	 * Returns the HTML content of an existing DOM element on the HTML page.
	 * 
	 * Should be careful with these type of methods if you are going to use the data
	 * later to ensure people are not injecting scripts into your code. In our
	 * example, we control the HTML that the data is retrieved from.
	 * 
	 * @param id The id of the DOM element we wish to get the content for.
	 * @return The HTML content of the DOM element.
	 */
	private String getContent(String id) {
		String toReturn = "";
		Element element = DOM.getElementById(id);

		if (element != null) {

			toReturn = DOM.getInnerHTML(element);
			DOM.setInnerText(element, "");
			SafeHtml sfHtml = SimpleHtmlSanitizer.sanitizeHtml(toReturn);
			toReturn = sfHtml.asString();
		} else {
			toReturn = "Unable to find " + id + " content in HTML page";
		}
		return toReturn;
	}

	TabLayoutPanel content;

	Button search;

	FocusPanel feedback;

	Image logo;

	/**
	 * Here we set up the logo by creating a new Image widget, and prevent the
	 * default browser action from occuring on it.
	 */
	private void insertLogo() {
		logo = new Image(GWT.getModuleBaseURL() + "../" + LOGO_IMAGE_NAME) {
			@Override
			public void onBrowserEvent(Event event) {
				event.preventDefault();
				super.onBrowserEvent(event);
			}
		};
	}

	/**
	 * Wrap the search button that already exists on the HTML page and store it as
	 * the previously declared search Button widget. If the button doesn't exist
	 * (you could, for example, edit the HTML page to take it away or change its id
	 * value to something else) then we'll log that fact and create it to avoid null
	 * pointer exceptions when accessing the button elsewhere in the application
	 * (like when adding event handlers to it).
	 */
	private void wrapExisitngSearchButton() {
		Element el = DOM.getElementById("search");

		if (el != null) {
			search = Button.wrap(el);
		} else {
			GWT.log("The search button is missing in the underlying HTML page, so we can't wrap it...trying to create it instead");
			search = new Button("search");
			RootPanel.get().add(search);
		}
	}

	/**
	 * Here we set up the event handling that we will drive user interaction.
	 * 
	 * 1. A SelectionHandler for when a new tab is selected. 2. A ClickHandler for
	 * if the search button is clicked. 3. Some Mouse handlers and ClickHandler if
	 * the feedback tab is interacted with.
	 * 
	 * You don't have to follow this style of programming and put all your event
	 * handling code into one method, we do it here as it makes sense and helps us
	 * examine particular aspects of code in one place (however, by doing it this
	 * way instead of, for example adding handlers directly after defining widgets,
	 * means we should check each widget is not null before adding the handler - we
	 * won't as by inspection we know all widgets are instantiated elsewhere before
	 * this method is called; but you should be aware of these type of dependencies
	 * in your own code).
	 * 
	 */
	private void setUpEventHandling() {

		/**
		 * If a tab is selected then we want to add a new history item to the History
		 * object. (this effectively changes the token in the URL, which is detected and
		 * handled by GWT's History sub-system.
		 */
		content.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				// Determine the tab that has been selected by interrogating the event object.
				Integer tabSelected = event.getSelectedItem();

				// Create a new history item for this tab (using data retrieved from Pages
				// enumeration)
				History.newItem(Pages.values()[tabSelected].getText());
			}
		});

		/**
		 * If the search button is clicked, we want to display a little pop-up panel
		 * which allows the user to type in a search term. The TextBox where the user
		 * types search terms should automatically gain focus to make it more user
		 * friendly.
		 */
		search.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				FlowPanel qAnswer;
				final TextBox searchTerm = new TextBox();

				// If search button is clicked for the first time then the searchRequest Pop-up
				// panel does not yet exist
				// so we'll build it first as follows:
				if (searchRequest == null) {
					// Create the PopupPanel widget
					searchRequest = new PopupPanel();

					// Create a FlowPanel to hold the question and answer for the search term
					qAnswer = new FlowPanel();
					// Add a Label to the Flow Panel that represents the "Search For" text
					qAnswer.add(new Label("Search For:"));
					// Add the answer TextBox (which we declared above) to the FlowPanel
					qAnswer.add(searchTerm);

					// Add a change handler to the TextBox so that when there is a change to search
					// term
					// we would "start" the search (we don't implement the search capability in this
					// simple example)
					searchTerm.addChangeHandler(new ChangeHandler() {
						public void onChange(ChangeEvent event) {
							// Hide the popup panel from the screen
							searchRequest.hide();
							// "start" the search
							Window.alert("If implemented, now we would search for: " + searchTerm.getText());
						}
					});

					// Add the question/answer to the search pop-up.
					searchRequest.add(qAnswer);

					// Now we'll set some properties on the pop up panel, we'll:
					// * indicate that the popup should be animated
					// * show it relative to the search button widget
					// * close it if the user clicks outside of it popup panel, or if the history
					// token is changed
					searchRequest.setAnimationEnabled(true);
					searchRequest.showRelativeTo(search);
					searchRequest.setAutoHideEnabled(true);
					searchRequest.setAutoHideOnHistoryEventsEnabled(true);
				} else {
					// search popup already exists, so clear the TextBox contents...
					searchTerm.setText("");
					// ... and simply show it.
					searchRequest.show();
				}

				// Set the TextBox of the popup Panel to have focus - this means that once the
				// pop up is displayed
				// then any keypresses the user makes will appear directly inthe TextBox. If we
				// didn't do this, then
				// who knows where the text would appear.
				searchTerm.setFocus(true);
			}
		});

		/**
		 * If the user moves mouse over feedback tab, change its style (increases its
		 * size and changes colour - styles are in BasicProject.css)
		 */
		feedback.addMouseOverHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				// Remove existing normal style
				feedback.removeStyleName("normal");
				// Add the active style
				feedback.addStyleName("active");
				// Set overflow of whole HTML page to hidden to minimise display of scroll bars.
				RootPanel.getBodyElement().getStyle().setProperty("overflow", "hidden");
			}
		});

		/**
		 * If use moves mouse out of the feedback panel, return its style to normal
		 * (decreases its size and changes colour - styles are in BasicProject.css)
		 */
		feedback.addMouseOutHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				feedback.removeStyleName("active");
				feedback.addStyleName("normal");
				RootPanel.getBodyElement().getStyle().setProperty("overflow", "auto");
			}
		});

		/**
		 * If user clicks on the feedback tab we should start some feedback
		 * functionality. In this example, it simply displays an alert to the user.
		 */
		feedback.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Window.alert("You could provide feedback if this was implemented");
			}
		});
	}

	HTMLPanel homePanel;
	HTMLPanel productsPanel;
	HTMLPanel contactPanel;

	/**
	 * We'll build the tab panel's content from the HTML that is already in the HTML
	 * page.
	 */
	private void buildTabContent() {
		GWT.debugger();
		homePanel = new HTMLPanel(getContent(Pages.HOME.getText()));
		productsPanel = new HTMLPanel(getContent(Pages.PRODUCTS.getText()));
		contactPanel = new HTMLPanel(getContent(Pages.CONTACT.getText()));

		homePanel.addStyleName("htmlPanel");
		productsPanel.addStyleName("htmlPanel");
		contactPanel.addStyleName("htmlPanel");

		content = new TabLayoutPanel(20, Unit.PX);

		content.add(homePanel, Pages.HOME.getText());
		content.add(productsPanel, Pages.PRODUCTS.getText());
		content.add(contactPanel, Pages.CONTACT.getText());

		content.selectTab(DECK_HOME);
	}

	/**
	 * Creating the Feedback tab
	 */
	private void createFeedbackTab() {
		// Create the FeedBack tab
		feedback = new FocusPanel();
		feedback.setStyleName("feedback");
		feedback.addStyleName("normal");
		// Create VerticalPanel that holds two labels "feed" and "back"
		VerticalPanel text = new VerticalPanel();
		text.add(new Label("Feed"));
		text.add(new Label("Back"));
		feedback.add(text);
	}

	/**
	 * Style the tab panel using methods in the UIObject class.
	 */
	private void styleTabPanelUsingUIObject() {
		// Set up the heights of the pages.
		homePanel.setHeight("400px");
		productsPanel.setHeight("400px");
		contactPanel.setHeight("400px");
		content.setHeight("420px");
	}

	/**
	 * Style the search button using DOM methods available through the
	 * Widget.getElement().getStyle() method.
	 */
	private void styleButtonUsingDOM() {
		// Set up some styling on the button
		search.getElement().getStyle().setProperty("backgroundColor", "#ff0000");
		search.getElement().getStyle().setProperty("border", "2px solid");
		search.getElement().getStyle().setOpacity(0.7);
	}

	/**
	 * Sets up the GUI components used in the application
	 * 
	 */
	private void setUpGui() {
		buildTabContent();
		wrapExisitngSearchButton();
		insertLogo();
		createFeedbackTab();

		styleTabPanelUsingUIObject();
		styleButtonUsingDOM();

		RootPanel.get().add(feedback);
		
		RootPanel logoSlot = RootPanel.get("logo");
		if (logoSlot != null) {
			logoSlot.add(logo);
		}
		
		RootPanel contentSlot = RootPanel.get("content");
		if (contentSlot != null) {
			contentSlot.add(content);
		}
	}

	/**
	 * This is the entry point method which will create the GUI and set up the
	 * History handling.
	 */
	public void onModuleLoad() {
		setUpGui();
		setUpHistoryManagement();
		setUpEventHandling();
	}

	/**
	 * Set up the History management for the application.
	 */
	public void setUpHistoryManagement() {
		// Make this class your history manager (see onValueChange method)
		History.addValueChangeHandler(this);
		// Handle any existing history token
		History.fireCurrentHistoryState();
		// Trap user hitting back button too many times.
		Window.addWindowClosingHandler(new ClosingHandler() {
			public void onWindowClosing(ClosingEvent event) {
				event.setMessage("Ran out of history.  Now leaving application, is that OK?");
			}
		});
	}

	/**
	 * This is the function that handles history change events.
	 * 
	 * When the history token is changed in the URL, GWT fires a ValueChangeEvent
	 * that is handled in this method (since we called
	 * History.addValueChangeHandler(this) in the onModuleLoad method).
	 * 
	 * The history token is the part of the URL that follows the hash symbol. For
	 * example http://www.someurl.se/MyApp.html#home has the token "home".
	 */
	public void onValueChange(ValueChangeEvent<String> event) {
		// Get the token from the event
		String page = event.getValue().trim();
		// Check if the token is null or empty
		if ((page == null) || (page.equals("")))
			showHomePage();
		// Else check what the token is and cal the appropriate method.
		else if (page.equals(Pages.PRODUCTS.getText()))
			showProducts();
		else if (page.equals(Pages.CONTACT.getText()))
			showContact();
		else
			showHomePage();
	}

	/**
	 * Show the contact page - i.e. place a new label on the current screen
	 */
	private void showContact() {
		content.selectTab(Pages.CONTACT.getVal());
	}

	/**
	 * Show the home page - i.e. place a new label on the current screen
	 */
	private void showHomePage() {
		content.selectTab(Pages.HOME.getVal());
	}

	/**
	 * Show the products page - i.e. place a new label on the current screen
	 */
	private void showProducts() {
		content.selectTab(Pages.PRODUCTS.getVal());
	}
}
