 package com.sickle.medu.ms.client.iportal.dialog;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Random;
 import com.sickle.medu.ms.client.datasource.MemberDataSource;
 import com.sickle.medu.ms.client.iportal.MeduIndexPage;
 import com.sickle.medu.ms.client.iportal.panel.RegisterPanel;
 import com.sickle.medu.ms.client.ui.IPageConst;
 import com.sickle.medu.ms.client.ui.dialog.AbstractDialog;
 import com.sickle.pojo.edu.Member;
 import com.smartgwt.client.data.DSCallback;
 import com.smartgwt.client.data.DSRequest;
 import com.smartgwt.client.data.DSResponse;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.util.SC;
 import com.smartgwt.client.widgets.Button;
 import com.smartgwt.client.widgets.Canvas;
 import com.smartgwt.client.widgets.events.ClickEvent;
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 
 
 public class ModifyMemberDialog extends AbstractDialog
 {
 	
 	private static final String TITLE = "修改个人资料";
 	
 	private RegisterPanel registerpanel;
 
 	private Member mem ; 
 	
 	public ModifyMemberDialog( Member member )
 	{
 		super( TITLE, true,true,true );
 		mem = member;
 		registerpanel.edit( mem );
 	}
 
 	@Override
 	public Canvas getView( )
 	{
 		VLayout mainpanel = new VLayout();
 		mainpanel.setWidth100( );
 		
 	    registerpanel = new RegisterPanel( 1 );
 		mainpanel.addMember( registerpanel );
 		
 		HLayout buttonpanel = new HLayout();
 		buttonpanel.setPadding( 10 );
 		buttonpanel.setWidth( 300 );
 		buttonpanel.setMembersMargin( 10 );
 		buttonpanel.setAlign( Alignment.CENTER );
 		
 		Button okbutton = new Button("确定修改");
 		okbutton.setWidth( 50 );
 		okbutton.addClickHandler( new ClickHandler( ) {
 			@Override
 			public void onClick( ClickEvent event )
 			{
 				registerpanel.precommit( );
 				boolean isValidate = registerpanel.getRegisterform().validate( );
 				if( isValidate == false)
 				{
 					return;
 				}
 				registerpanel.getRegisterform().submit( new DSCallback(){
 					@Override
 					public void execute( DSResponse dsResponse, Object data,
 							DSRequest dsRequest )
 					{
 						SC.say( "修改成功" );
 						Member returnmember =  new Member();
 						ListGridRecord rec = new ListGridRecord( dsRequest.getData( ) );
 						MemberDataSource.getInstance( ).recopyValues( rec  , returnmember);
 						MeduIndexPage.getInstance( ).getTopbar( ).setMember( returnmember );
						History.newItem( IPageConst.PAGE_MS + Random.nextDouble( ) );
 						hide();
 					}
 				} );
 			
 			}
 		} );
 		
 		Button cancelbutton = new Button("取消");
 		cancelbutton.setWidth( 50 );
 		cancelbutton.addClickHandler( new ClickHandler( ) {
 			@Override
 			public void onClick( ClickEvent event )
 			{
 				registerpanel.getRegisterform().reset( );
 				hide();
 			}
 		} );
 		
 		buttonpanel.addMember( okbutton );
 		buttonpanel.addMember( cancelbutton );
 		
 		mainpanel.addMember( buttonpanel );
 		return mainpanel;
 	}
 
 }
