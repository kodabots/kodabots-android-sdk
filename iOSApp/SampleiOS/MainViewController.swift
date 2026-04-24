//
//  MainViewController.swift
//  KodaBotsSample
//
//  Created by Felislynx.Silae on 12/10/2020.
//  Copyright © 2020 KodaBots. All rights reserved.
//

import KodaBotsKit
import UIKit

final class MainViewController: UIViewController {

	// MARK: - Properties (private)

	private var customClientToken: String?
	private var customBaseUrl: String?
	private var customBaseRestUrl: String?

	// MARK: - Properties

	var kodaBotsWebView: UIViewController?
	var kodaBotsWebViewHandle: KodaBotsWebViewScreenHandle?
	var callbacks: (KodaBotsCallback) -> Void = { callback in
		if callback is KodaBotsError {
			let it = callback as! KodaBotsError
			print("KodaBotsSDK -> Error received: \(it.error)")
		}
		if callback is KodaBotsEvent {
			let it = callback as! KodaBotsEvent
			print("KodaBotsSDK -> Event received: \(it.type) - \(it.params)")
		}
	}

	// MARK: - IBOUtlet

	@IBOutlet weak var controllsButton: UIButton!
	@IBOutlet weak var webViewContainer: UIView!

	override func viewDidLoad() {
		super.viewDidLoad()
		controllsButton.addTarget(
			self,
			action: #selector(onControllsClicked(_:)),
			for: .touchUpInside
		)
	}

	@objc
	func onControllsClicked(_ sender: Any?) {
		let optionMenu = UIAlertController(
			title: nil,
			message: nil,
			preferredStyle: .actionSheet
		)
		let initializeAction = UIAlertAction(
			title: NSLocalizedString("INITIALIZE WEBVIEW", comment: ""),
			style: .default
		) { (action) in
			self.initializeWebview()
		}
		let setClientIDAction = UIAlertAction(
			title: NSLocalizedString("SET CLIENT ID", comment: ""),
			style: .default
		) { (action) in
			let alert = UIAlertController(
				title: "Set Client ID",
				message: "",
				preferredStyle: .alert
			)
			alert.addTextField { (textField) in
				textField.placeholder = "Client ID"
			}
			alert.addAction(
				UIAlertAction(
					title: "Set",
					style: .default,
					handler: { [weak alert] (_) in
						let clientID = alert?.textFields?[0].text
						guard let clientID else { return }
						guard !clientID.isEmpty else { return }
						self.customClientToken = clientID
						self.showToast("CLIENT ID SET: \(clientID)")
					}
				)
			)
			alert.addAction(
				UIAlertAction(
					title: "Cancel",
					style: .default,
					handler: { _ in
						optionMenu.dismiss(animated: true, completion: nil)
					}
				)
			)
			self.present(alert, animated: true, completion: nil)
		}
		let getUnreadCountAction = UIAlertAction(
			title: NSLocalizedString("GET UNREAD COUNT", comment: ""),
			style: .default
		) { (action) in
			KodaBotsSDK.shared.getUnreadCount(callback: { response in
				switch response {
				case let success as CallResponseSuccess<KotlinInt>:
					self.showToast("Unread messages: \(success.value ?? 0)")
				case let error as CallResponseError<KotlinInt>:
					print("KodaBotsSample -> Error \(error)")
					self.showToast("Error \(error)")
				case is CallResponseTimeout<KotlinInt>:
					print("KodaBotsSample -> Timeout")
					self.showToast("TIMEOUT")
				default:
					print("KodaBotsSample -> Unknown error")
					self.showToast("Unexpected error!")
				}
			})
		}
		let syncProfileAction = UIAlertAction(
			title: NSLocalizedString("SYNC PROFILE", comment: ""),
			style: .default
		) { (action) in
			let alert = UIAlertController(
				title: "Sync profile",
				message: "",
				preferredStyle: .alert
			)
			alert.addTextField { (textField) in
				textField.text = "First Name"
			}
			alert.addTextField { (textField) in
				textField.text = "Last Name"
			}
			alert.addTextField { (textField) in
				textField.text = "Custom key"
			}
			alert.addAction(
				UIAlertAction(
					title: "OK",
					style: .default,
					handler: { [weak alert, weak self] (_) in
						guard let self else { return }
						guard kodaBotsWebViewHandle != nil else {
							self.showToast("kodaBotsWebView is nil")
							return
						}
						let profile = UserProfile()
						profile.first_name = alert?.textFields![0].text
						profile.last_name = alert?.textFields![1].text
						profile.custom_parameters["custom_key"] = alert?.textFields![2].text
						if self.kodaBotsWebViewHandle!.syncUserProfile(profile: profile)
							== false
						{
							self.showToast("INITIALIZE WEBVIEW")
						}
					}
				)
			)
			self.present(alert, animated: true, completion: nil)
		}
		let sendBlockAction = UIAlertAction(
			title: NSLocalizedString("SET BLOCK ID", comment: ""),
			style: .default
		) { (action) in
			let alert = UIAlertController(
				title: "Set blockId",
				message: "",
				preferredStyle: .alert
			)
			alert.addTextField { (textField) in
				textField.placeholder = "Block ID"
			}
			alert.addTextField { (textField) in
				textField.placeholder = "Param Key (Optional)"
			}
			alert.addTextField { (textField) in
				textField.placeholder = "Param Value (Optional)"
			}
			alert.addAction(
				UIAlertAction(
					title: "OK",
					style: .default,
					handler: { [weak alert] (_) in
						if self.kodaBotsWebView != nil {
							let blockID = alert?.textFields?[0].text ?? ""
							let paramKey = alert?.textFields?[1].text
							let paramValue = alert?.textFields?[2].text
							var params: [String: String]? = nil
							if let paramKey, !paramKey.isEmpty,
								let paramValue, !paramValue.isEmpty
							{
								params = [paramKey: paramValue]
							}
							if !self.kodaBotsWebViewHandle!.sendBlock(
								blockId: blockID,
								params: params
							) {
								self.showToast("INITIALIZE WEBVIEW")
							}
						} else {
							self.showToast("INITIALIZE WEBVIEW")
						}
					}
				)
			)
			self.present(alert, animated: true, completion: nil)
		}
		let simulateAlertAction = UIAlertAction(
			title: NSLocalizedString("SIMULATE ERROR", comment: ""),
			style: .default
		) { (action) in
			if self.kodaBotsWebView != nil {
				if self.kodaBotsWebViewHandle!.simulateError() == false {
					self.showToast("INITIALIZE WEBVIEW")
				}
			} else {
				self.showToast("INITIALIZE WEBVIEW")
			}
		}
		let closeAction = UIAlertAction(
			title: NSLocalizedString("Close", comment: ""),
			style: .default
		) { (action) in
			optionMenu.dismiss(animated: true, completion: nil)
		}
		let setCustomUrlsAction = UIAlertAction(
			title: NSLocalizedString("SET CUSTOM URLS", comment: ""),
			style: .default
		) { (action) in
			let alert = UIAlertController(
				title: "Set Custom URLs",
				message: nil,
				preferredStyle: .alert
			)
			alert.addTextField { textField in
				textField.placeholder = "Custom Base URL (optional)"
				textField.text = self.customBaseUrl
			}
			alert.addTextField { textField in
				textField.placeholder = "Custom REST URL (optional)"
				textField.text = self.customBaseRestUrl
			}
			alert.addAction(UIAlertAction(title: "Set", style: .default) { [weak alert] _ in
				self.customBaseUrl = alert?.textFields?[0].text?.isEmpty == false
					? alert?.textFields?[0].text : nil
				self.customBaseRestUrl = alert?.textFields?[1].text?.isEmpty == false
					? alert?.textFields?[1].text : nil
				self.showToast("URLs updated")
			})
			alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
			self.present(alert, animated: true, completion: nil)
		}
		optionMenu.addAction(initializeAction)
		optionMenu.addAction(setClientIDAction)
		optionMenu.addAction(setCustomUrlsAction)
		optionMenu.addAction(getUnreadCountAction)
		optionMenu.addAction(syncProfileAction)
		optionMenu.addAction(sendBlockAction)
		optionMenu.addAction(simulateAlertAction)
		optionMenu.addAction(closeAction)
		present(optionMenu, animated: true, completion: nil)
	}

	func showToast(_ message: String) {
		DispatchQueue.main.async {
			let alert = UIAlertController(
				title: message,
				message: "",
				preferredStyle: UIAlertController.Style.alert
			)
			self.present(alert, animated: true, completion: nil)
			DispatchQueue.main.asyncAfter(deadline: .now() + 2.75) {
				alert.dismiss(animated: true, completion: nil)
			}
		}
	}

	func initializeWebview() {
		let workItem = DispatchWorkItem {
			guard self.initializeKodaBot() else { return }
			if let viewController = KodaBotsSDK.shared.generateScreen()
				as? UIViewController
			{
				self.kodaBotsWebView = viewController
				self.kodaBotsWebViewHandle = KodaBotsWebViewScreenHandle(
					webViewScreen: viewController
				)
				self.webViewContainer.addSubview(viewController.view)
				self.addChild(viewController)
				viewController.didMove(toParent: self)
				viewController.view.translatesAutoresizingMaskIntoConstraints = false
				NSLayoutConstraint.activate([
					viewController.view.bottomAnchor.constraint(
						equalTo: self.webViewContainer.bottomAnchor
					),
					viewController.view.topAnchor.constraint(
						equalTo: self.webViewContainer.topAnchor
					),
					viewController.view.leadingAnchor.constraint(
						equalTo: self.webViewContainer.leadingAnchor
					),
					viewController.view.trailingAnchor.constraint(
						equalTo: self.webViewContainer.trailingAnchor
					),
				])
			}
		}
		DispatchQueue.main.async(execute: workItem)
	}

	func initializeKodaBot() -> Bool {
		let timeoutImage = UIImage(named: "went_wrong")
		let timeoutConfig = KodaBotsTimedOutConfig()
		timeoutConfig.buttonColor = UIColor.magenta
		timeoutConfig.image = timeoutImage
		timeoutConfig.timeout = 20
		timeoutConfig.message = "Something went wrong!"
		
        let progressConfig = KodaBotsProgressConfig(
            backgroundColor: UIColor.white,
            progressColor: UIColor.red,
            customAnimation: nil,
        )
		let config = KodaBotsConfig.init(
			userProfile: UserProfile(),
			blockId: nil,
			progressConfig: progressConfig,
			timeoutConfig: timeoutConfig,
			customClientToken: customClientToken,
            customBaseUrl: customBaseUrl,
            customBaseRestUrl: customBaseRestUrl
		)

		let driver = IosKodaBotsSDKDriver(
			config: config,
			callbacks: callbacks
		)
		return KodaBotsSDK.shared.doInit(driver: driver)
	}
}
