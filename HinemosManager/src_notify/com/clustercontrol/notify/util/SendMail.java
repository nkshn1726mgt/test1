/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MailTemplateNotFound;
import com.clustercontrol.fault.NotifyNotFound;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.notify.bean.NotifyRequestMessage;
import com.clustercontrol.notify.bean.OutputBasicInfo;
import com.clustercontrol.notify.mail.model.MailTemplateInfo;
import com.clustercontrol.notify.mail.session.MailTemplateControllerBean;
import com.clustercontrol.notify.model.NotifyMailInfo;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.StringBinder;
import com.clustercontrol.util.apllog.AplLogger;
import com.sun.mail.smtp.SMTPAddressFailedException;

/**
 * ?????????????????????????????????<BR>
 *
 * @version 3.0.0
 * @since 3.0.0
 */
public class SendMail implements Notifier {

	/** ???????????????????????????????????? */
	private static Log m_log = LogFactory.getLog(SendMail.class);

	/** ??????????????????????????? */
	private static final String SUBJECT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * ????????????????????????????????????
	 *
	 * @param outputInfo ?????????????????????
	 */
	@Override
	public synchronized void notify(NotifyRequestMessage message) {

		sendMail(message.getOutputInfo(), message.getNotifyId());
	}

	/**
	 * ????????????????????????????????????
	 *
	 */
	private void sendMail(OutputBasicInfo outputInfo, String notifyId) {

		if (m_log.isDebugEnabled()) {
			m_log.debug("sendMail() " + outputInfo);
		}

		try {
			NotifyMailInfo mailInfo = QueryUtil.getNotifyMailInfoPK(notifyId);

			// ???????????????????????????
			String subject = getSubject(outputInfo, mailInfo);

			// ???????????????????????????
			String content = getContent(outputInfo, mailInfo);

			/**
			 * ???????????????
			 */
			String address = null;
			switch (outputInfo.getPriority()) {
			case PriorityConstant.TYPE_INFO:
				address = mailInfo.getInfoMailAddress();
				break;
			case PriorityConstant.TYPE_WARNING:
				address = mailInfo.getWarnMailAddress();
				break;
			case PriorityConstant.TYPE_CRITICAL:
				address = mailInfo.getCriticalMailAddress();
				break;
			case PriorityConstant.TYPE_UNKNOWN:
				address = mailInfo.getUnknownMailAddress();
				break;
			default:
				break;
			}
			if (address == null) {
				m_log.info("address is null");
				return;
			}
			if (address.length() == 0) {
				m_log.info("address.length()==0");
				return;
			}

			String changeAddress = null;
			try {
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				ArrayList<String> inKeyList = StringBinder.getKeyList(address, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(outputInfo, inKeyList);
				StringBinder binder = new StringBinder(param);
				changeAddress = binder.bindParam(address);
			} catch (Exception e) {
				m_log.warn("sendMail() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				changeAddress = address;
			}
			StringTokenizer t = new StringTokenizer(changeAddress, ";");
			ArrayList<String> toAddressList = new ArrayList<String>();
			ArrayList<String> ccAddressList = new ArrayList<String>();
			ArrayList<String> bccAddressList = new ArrayList<String>();
			String separator = ":";
			String ccPrefix = "CC" + separator;
			String bccPrefix = "BCC" + separator;
			while (t.hasMoreTokens()) {
				String addr = t.nextToken();
				if (addr.startsWith(ccPrefix)) {
					ccAddressList.add(addr.substring(ccPrefix.length()));
				} else if (addr.startsWith(bccPrefix)) {
					bccAddressList.add(addr.substring(bccPrefix.length()));
				} else {
					toAddressList.add(addr);
				}
			}
			String[] toAddress = toAddressList.toArray(new String[0]);

			if (toAddress == null || toAddress.length <= 0) {
				m_log.debug("sendMail() : mail address is empty");
				return;
			}

			try {
				this.sendMail(toAddress, ccAddressList.toArray(new String[0]), bccAddressList.toArray(new String[0]), subject, content);
			} catch (AuthenticationFailedException e) {
				String detailMsg = "cannot connect to the mail server due to an Authentication Failure";
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				internalErrorNotify(PriorityConstant.TYPE_CRITICAL, notifyId, MessageConstant.MESSAGE_SYS_007_NOTIFY, detailMsg);
			} catch (SMTPAddressFailedException e) {
				String detailMsg = e.getMessage() + "(SMTPAddressFailedException)";
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				internalErrorNotify(PriorityConstant.TYPE_CRITICAL, notifyId, MessageConstant.MESSAGE_SYS_007_NOTIFY, detailMsg);
			} catch (MessagingException e) {
				String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage() : e.getMessage();
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				internalErrorNotify(PriorityConstant.TYPE_CRITICAL, notifyId, MessageConstant.MESSAGE_SYS_007_NOTIFY, detailMsg);
			} catch (UnsupportedEncodingException e) {
				String detailMsg = e.getCause() != null ? e.getMessage() + " Cause : " + e.getCause().getMessage() : e.getMessage();
				m_log.warn("sendMail() " + e.getMessage() + " : " + detailMsg + detailMsg + " : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				internalErrorNotify(PriorityConstant.TYPE_CRITICAL, notifyId, MessageConstant.MESSAGE_SYS_007_NOTIFY, detailMsg);
			}
		} catch (RuntimeException | NotifyNotFound e1) {
			String detailMsg = e1.getCause() != null ? e1.getMessage() + " Cause : " + e1.getCause().getMessage() : e1.getMessage();
			m_log.warn("sendMail() " + e1.getMessage() + " : " + detailMsg + detailMsg + " : "
					+ e1.getClass().getSimpleName() + ", " + e1.getMessage(), e1);
			internalErrorNotify(PriorityConstant.TYPE_CRITICAL, notifyId, MessageConstant.MESSAGE_SYS_007_NOTIFY, detailMsg);
		}
	}

	/**
	 * ??????????????????????????????
	 *
	 * <p>
	 * ?????????????????????????????????????????????????????????
	 * <p>
	 * <ul>
	 * <li>?????????????????????</li>
	 * <li>??????????????????</li>
	 * <li>??????????????????????????????</li>
	 * <li>???????????????????????????</li>
	 * <li>??????????????????????????????</li>
	 * </ul>
	 *
	 * @param addressTo
	 *            ?????????????????????
	 * @param source
	 *            ????????????
	 * @return ??????????????????????????????<code> true </code>
	 * @throws MessagingException
	 * @throws NamingException
	 * @throws UnsupportedEncodingException
	 */
	public void sendMail(String[] toAddressStr, String subject, String content)
			throws MessagingException, UnsupportedEncodingException {
		sendMail(toAddressStr, null, null, subject, content);
	}

	public void sendMail(String[] toAddressStr, String[] ccAddressStr, String[] bccAddressStr, String subject, String content)
			throws MessagingException, UnsupportedEncodingException {

		if (toAddressStr == null || toAddressStr.length <= 0) {
			// ??????????????????
			return;
		}

		/*
		 *  https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html
		 */
		Properties _properties = new Properties();
		_properties.setProperty("mail.debug", Boolean.toString(HinemosPropertyCommon.mail_debug.getBooleanValue()));
		_properties.setProperty("mail.store.protocol", HinemosPropertyCommon.mail_store_protocol.getStringValue());
		String protocol = HinemosPropertyCommon.mail_transport_protocol.getStringValue();
		_properties.setProperty("mail.transport.protocol", protocol);
		_properties.put("mail.smtp.socketFactory", javax.net.SocketFactory.getDefault());
		_properties.put("mail.smtp.ssl.socketFactory", javax.net.ssl.SSLSocketFactory.getDefault());
		
		setProperties(_properties, HinemosPropertyCommon.mail_$_user, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_host, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_port, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_connectiontimeout, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_timeout, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_writetimeout, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_from, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_localhost, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_localaddress, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_localport, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_ehlo, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_mechanisms, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_login_disable, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_plain_disable, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_digest_md5_disable, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_ntlm_disable, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_ntlm_domain, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_auth_ntlm_flags, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_submitter, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_dsn_notify, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_dsn_ret, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_allow8bitmime, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_sendpartial, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_sasl_enable, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_sasl_mechanisms, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_sasl_authorizationid, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_sasl_realm, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_sasl_usecanonicalhostname, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_quitwait, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_reportsuccess, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_socketFactory_class, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_socketFactory_fallback, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_socketFactory_port, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_starttls_enable, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_starttls_required, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_socks_host, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_socks_port, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_mailextension, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_userset, protocol);
		setProperties(_properties, HinemosPropertyCommon.mail_$_noop_strict, protocol);
		
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_enable, "");
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_checkserveridentity, "");
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_trust, "");
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_socketFactory_class, "");
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_socketFactory_port, "");
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_protocols, "");
		setProperties(_properties, HinemosPropertyCommon.mail_smtp_ssl_ciphersuites, "");

		/**
		 * ?????????????????????DB????????????
		 */
		String _loginUser = HinemosPropertyCommon.mail_transport_user.getStringValue();
		String _loginPassword = HinemosPropertyCommon.mail_transport_password.getStringValue();
		String _fromAddress = HinemosPropertyCommon.mail_from_address.getStringValue();
		String _fromPersonalName = HinemosPropertyCommon.mail_from_personal_name.getStringValue();
		_fromPersonalName = convertNativeToAscii(_fromPersonalName);
		
		String _replyToAddress = HinemosPropertyCommon.mail_reply_to_address.getStringValue();
		String _replyToPersonalName =HinemosPropertyCommon.mail_reply_personal_name.getStringValue();
		_replyToPersonalName = convertNativeToAscii(_replyToPersonalName);
		
		String _errorsToAddress = HinemosPropertyCommon.mail_errors_to_address.getStringValue();

		int _transportTries = HinemosPropertyCommon.mail_transport_tries.getIntegerValue();
		int _transportTriesInterval = HinemosPropertyCommon.mail_transport_tries_interval.getIntegerValue();

		String _charsetAddress = HinemosPropertyCommon.mail_charset_address.getStringValue();
		String _charsetSubject = HinemosPropertyCommon.mail_charset_subject.getStringValue();
		String _charsetContent = HinemosPropertyCommon.mail_charset_content.getStringValue();

		m_log.debug("initialized mail sender : from_address = " + _fromAddress
				+ ", From = " + _fromPersonalName + " <" + _replyToAddress + ">"
				+ ", Reply-To = " + _replyToPersonalName + " <" + _replyToAddress + ">"
				+ ", Errors-To = " + _errorsToAddress
				+ ", tries = " + _transportTries
				+ ", tries-interval = " + _transportTriesInterval
				+ ", Charset [address:subject:content] = [" + _charsetAddress + ":" + _charsetSubject + ":" + _charsetContent + "]");

		// JavaMail Session??????????????????
		Session session = Session.getInstance(_properties);

		Message mineMsg = new MimeMessage(session);

		// ??????????????????????????????????????????????????????
		if (_fromAddress != null && _fromPersonalName != null) {
			mineMsg.setFrom(new InternetAddress(_fromAddress, _fromPersonalName, _charsetAddress));
		} else if (_fromAddress != null && _fromPersonalName == null) {
			mineMsg.setFrom(new InternetAddress(_fromAddress));
		}
		// REPLY-TO?????????
		if (_replyToAddress != null && _replyToPersonalName != null) {
			InternetAddress reply[] = { new InternetAddress(_replyToAddress, _replyToPersonalName, _charsetAddress) };
			mineMsg.setReplyTo(reply);
			mineMsg.reply(true);
		} else if (_replyToAddress != null && _replyToPersonalName == null) {
			InternetAddress reply[] = { new InternetAddress(_replyToAddress) };
			mineMsg.setReplyTo(reply);
			mineMsg.reply(true);
		}

		// ERRORS-TO?????????
		if (_errorsToAddress != null) {
			mineMsg.setHeader("Errors-To", _errorsToAddress);
		}

		// ???????????????????????????????????????
		// TO
		InternetAddress[] toAddress = this.getAddress(toAddressStr);
		if (toAddress != null && toAddress.length > 0) {
			mineMsg.setRecipients(javax.mail.Message.RecipientType.TO, toAddress);
		} else {
			return; // TO?????????
		}
		// CC
		if (ccAddressStr != null) {
			InternetAddress[] ccAddress = this.getAddress(ccAddressStr);
			if (ccAddress != null && ccAddress.length > 0) {
				mineMsg.setRecipients(javax.mail.Message.RecipientType.CC, ccAddress);
			}
		}
		// BCC
		if (bccAddressStr != null) {
			InternetAddress[] bccAddress = this.getAddress(bccAddressStr);
			if (bccAddress != null && bccAddress.length > 0) {
				mineMsg.setRecipients(javax.mail.Message.RecipientType.BCC, bccAddress);
			}
		}
		String message = "TO=" + Arrays.asList(toAddressStr);
		
		if (ccAddressStr != null) {
			message += ", CC=" + Arrays.asList(ccAddressStr);
		}
		if (bccAddressStr != null) {
			message += ", BCC=" + Arrays.asList(bccAddressStr);
		}
		m_log.debug(message);

		// ???????????????????????????
		mineMsg.setSubject(MimeUtility.encodeText(subject, _charsetSubject, "B"));

		// ???????????????????????????
		mineMsg.setContent(content, "text/plain; charset=" + _charsetContent);

		// ?????????????????????
		mineMsg.setSentDate(HinemosTime.getDateInstance());

		// ?????????????????????true?????????????????????????????????
		for (int i = 0; i < _transportTries; i++) {
			Transport transport = null;
			try {
				// ???????????????
				transport = session.getTransport();
				boolean flag = HinemosPropertyCommon.mail_$_auth.getBooleanValue(protocol, false);
				if(flag) {
					transport.connect(_loginUser, _loginPassword);
				} else {
					transport.connect();
				}
				transport.sendMessage(mineMsg, mineMsg.getAllRecipients());
				break;
			} catch (AuthenticationFailedException e) {
				throw e;
			} catch (SMTPAddressFailedException e) {
				throw e;
			} catch (MessagingException me) {
				//_transportTries??????sleep???????????? 
				if (i < (_transportTries - 1)) { 
					m_log.info("sendMail() : retry sendmail. " + me.getMessage());
					try {
						Thread.sleep(_transportTriesInterval);
					} catch (InterruptedException e) { }
				//_transportTries????????????INTERNAL??????????????????????????????Exception???throw 
				} else {
					throw me;
				}
			} finally {
				if (transport != null) {
					transport.close();
				}
			}
		}
	}
	
	private String convertNativeToAscii(String nativeStr) {
		if (HinemosPropertyCommon.mail_native_to_ascii.getBooleanValue()){
			final CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
			final StringBuilder asciiStr = new StringBuilder();
			for (final Character character : nativeStr.toCharArray()) {
				if (asciiEncoder.canEncode(character)) {
					asciiStr.append(character);
				} else {
					asciiStr.append("\\u");
					asciiStr.append(Integer.toHexString(0x10000 | character).substring(1));
				}
			}
			
			return asciiStr.toString();
		} else {
			return nativeStr;
		}
	}

	/**
	 * ????????????????????????????????????????????????<code> InternetAddress </code>????????????????????????????????????
	 *
	 * @param addressList
	 *            ???????????????????????????????????????
	 * @return <code> InternetAddress </code>???????????????????????????
	 */
	private InternetAddress[] getAddress(String[] addressList) {
		InternetAddress toAddress[] = null;
		Vector<InternetAddress> list = new Vector<InternetAddress>();
		if (addressList != null) {
			for (String address : addressList) {
				try {
					list.add(new InternetAddress(address));
				} catch (AddressException e) {
					m_log.info("getAddress() : "
							+ e.getClass().getSimpleName() + ", "
							+ address + ", "
							+ e.getMessage());
				}
			}
			if (list.size() > 0) {
				toAddress = new InternetAddress[list.size()];
				list.copyInto(toAddress);
			}
		}
		return toAddress;
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param source
	 *            ????????????
	 * @param mailInfo
	 *            ????????????
	 * @return ???????????????
	 */
	public String getSubject(OutputBasicInfo source,
			NotifyMailInfo mailInfo) {

		String subject = null;
		try {
			if (mailInfo != null
					&& mailInfo.getMailTemplateInfoEntity() != null
					&& mailInfo.getMailTemplateInfoEntity().getMailTemplateId() != null) {
				MailTemplateInfo templateData
				= new MailTemplateControllerBean().getMailTemplateInfo(
						mailInfo.getMailTemplateInfoEntity().getMailTemplateId());
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String origin = templateData.getSubject();
				ArrayList<String> inKeyList = StringBinder.getKeyList(origin, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(source, mailInfo.getNotifyInfoEntity(), inKeyList);
				StringBinder binder = new StringBinder(param);
				subject = binder.replace(origin);
			} else {
				Locale locale = NotifyUtil.getNotifyLocale();
				subject = Messages.getString("MAIL_SUBJECT", locale) + "("
						+ Messages.getString(PriorityConstant.typeToMessageCode(source.getPriority()), locale)
						+ ")";
			}
		} catch (Exception e) {
			m_log.warn("getSubject() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// ?????????????????????????????????????????????
			return "Hinemos Notification";
		}

		return subject;
	}

	/**
	 * ?????????????????????????????????
	 *
	 * @param source
	 *            ????????????
	 * @param mailInfo
	 *            ????????????
	 * @return ???????????????
	 */
	public String getContent(OutputBasicInfo source, NotifyMailInfo mailInfo) {

		StringBuffer buf = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat(SUBJECT_DATE_FORMAT);
		sdf.setTimeZone(HinemosTime.getTimeZone());

		try {
			if (mailInfo != null
					&& mailInfo.getMailTemplateInfoEntity() != null
					&& mailInfo.getMailTemplateInfoEntity().getMailTemplateId() != null) {
				MailTemplateInfo mailData
				= new MailTemplateControllerBean().getMailTemplateInfo(
						mailInfo.getMailTemplateInfoEntity().getMailTemplateId());
				int maxReplaceWord = HinemosPropertyCommon.replace_param_max.getIntegerValue().intValue();
				String origin = mailData.getBody();
				ArrayList<String> inKeyList = StringBinder.getKeyList(origin, maxReplaceWord);
				Map<String, String> param = NotifyUtil.createParameter(source,
						mailInfo.getNotifyInfoEntity(), inKeyList);
				StringBinder binder = new StringBinder(param);
				buf.append(binder.replace(origin + "\n"));
			} else {
				
				Locale locale = NotifyUtil.getNotifyLocale();
				
				buf.append(Messages.getString("GENERATION_TIME", locale)
						+ " : "
						+ sdf.format(source.getGenerationDate()) + "\n");
				buf.append(Messages.getString("APPLICATION", locale) + " : "
						+ HinemosMessage.replace(source.getApplication(), locale) + "\n");
				buf.append(Messages.getString("PRIORITY", locale) + " : "
						+ Messages.getString(PriorityConstant.typeToMessageCode(source.getPriority()), locale)
						+ "\n");
				buf.append(Messages.getString("MESSAGE", locale) + " : "
						+ HinemosMessage.replace(source.getMessage(), locale) + "\n");
				buf.append(Messages.getString("SCOPE", locale) + " : "
						+ HinemosMessage.replace(source.getScopeText(), locale) + "\n");
			}
		} catch (MailTemplateNotFound | InvalidRole | HinemosUnknown | RuntimeException e) {
			m_log.warn("getContent() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			// ?????????????????????????????????
			return "An error occurred creating message.";
		}

		// ??????????????????LF??????CRLF??????????????????
		// ?????????JavaMail????????????????????????????????????????????????????????????????????????
		// ???????????????????????????
		String ret = buf.toString().replaceAll("\r\n", "\n").replaceAll("\n", "\r\n");

		return ret;
	}

	/**
	 * ?????????????????????????????????????????????????????????
	 */
	@Override
	public void internalErrorNotify(int priority, String notifyId, MessageConstant msgCode, String detailMsg) {
		String[] args = { notifyId };
		// ????????????????????????????????????
		AplLogger.put(priority, HinemosModuleConstant.PLATFORM_NOTIFY,  msgCode, args, detailMsg);
	}

	private void setProperties(Properties prop, HinemosPropertyCommon hinemosPropertyCommon, String replaceStr) {
		switch (hinemosPropertyCommon.getBean().getType()) {
		case HinemosPropertyTypeConstant.TYPE_STRING:
			String strVal = hinemosPropertyCommon.getStringValue(replaceStr, null);
			if (strVal != null) {
				prop.setProperty(hinemosPropertyCommon.getReplaceKey(replaceStr), strVal);
			}
			break;
		case HinemosPropertyTypeConstant.TYPE_NUMERIC:
			Long longVal = hinemosPropertyCommon.getNumericValue(replaceStr, null);
			if (longVal != null) {
				prop.setProperty(hinemosPropertyCommon.getReplaceKey(replaceStr), longVal.toString());
			}
			break;
		case HinemosPropertyTypeConstant.TYPE_TRUTH:
			Boolean boolVal = hinemosPropertyCommon.getBooleanValue(replaceStr, null);
			if (boolVal != null) {
				prop.setProperty(hinemosPropertyCommon.getReplaceKey(replaceStr), boolVal.toString());
			}
			break;
		default:
			//?????????????????????????????????
			break;
		}
	}
}
