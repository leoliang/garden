package org.unidal.orchid.uml.help;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.unidal.orchid.service.UmlService;
import org.unidal.orchid.uml.UmlPage;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UmlService m_manager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "help")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "help")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		String umlFile = payload.getUmlFile();

		model.setAction(Action.VIEW);
		model.setPage(UmlPage.HELP);

		if (umlFile != null) {
			showImage(ctx, model, umlFile);
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void showImage(Context ctx, Model model, String umlFile) throws IOException {
		HttpServletResponse res = ctx.getHttpServletResponse();
		InputStream in = getClass().getResourceAsStream("/help" + umlFile);

		if (in != null) {
			String uml = Files.forIO().readFrom(in, "utf-8");
			String type = null;

			res.setContentType(m_manager.getContextType(uml, type));
			type = m_manager.getImageType(type);

			byte[] image = m_manager.generateImage(uml, type);

			if (image != null) {
				res.setContentLength(image.length);
				res.getOutputStream().write(image);
			} else {
				res.sendError(400, "UML Incompleted!");
			}
			;
		} else {
			res.sendError(404, "File Not Found(/help" + umlFile + ")!");
		}

		ctx.stopProcess();
	}
}
