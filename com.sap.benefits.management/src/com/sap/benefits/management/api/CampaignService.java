package com.sap.benefits.management.api;

import static java.text.MessageFormat.format;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sap.benefits.management.persistence.CampaignDAO;
import com.sap.benefits.management.persistence.model.Campaign;
import com.sap.benefits.management.persistence.model.User;

@Path("/campaigns")
public class CampaignService extends BaseService{
	
	private CampaignDAO campaignDAO = new CampaignDAO();

	@GET
	@Path("/active")
	@Produces(MediaType.APPLICATION_JSON)
	public Campaign getActiveCampaignForCurrentUser(){
		final User currentUser = getLoggedInUser();
		return campaignDAO.getActiveCampaign(currentUser);
	}	
	
	@GET
	@Path("/admin")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<Campaign> getCampaigns(){
		final User user = getLoggedInUser();
		final Collection<Campaign> campaigns = user.getCampaigns();
		
		return campaigns;
	}
	
	@POST
	@Path("/admin")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addCampaign(Campaign campaign){
		final User user = getLoggedInUser();
		if(campaignDAO.getByName(campaign.getName(), user) != null){
			return createBadRequestResponse(format("Campaign with name \"{0}\" already exist", campaign.getName()));
		}else if(campaign.isActive() && !campaignDAO.canBeActive(campaign, user)){
			return createBadRequestResponse("Another campaign is set as active");
		}
		
		campaign.setOwner(user);
		campaignDAO.saveNew(campaign);
		campaignDAO.setPointsToUsers(campaign);
		
		return createOkResponse();
	}
	
	@POST
	@Path("/admin/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editCampaign(@PathParam("id") long id, Campaign campaign){
		final User user = getLoggedInUser();
		final Campaign camp = campaignDAO.getById(id);
		if(camp == null){
			return createBadRequestResponse("Campaign does not exist");
		}else if(campaign.isActive() && !campaignDAO.canBeActive(campaign, user)){
			return createBadRequestResponse("Another campaign is set as active");
		}
		
		camp.setName(campaign.getName());
		camp.setStartDate(campaign.getStartDate());
		camp.setEndDate(campaign.getEndDate());
		camp.setActive(campaign.isActive());
		camp.setPoints(campaign.getPoints());
		
		campaignDAO.save(camp);
		return createOkResponse();
	}
	
}
