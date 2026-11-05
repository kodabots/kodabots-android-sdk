//
//  SceneDelegate.swift
//  KodaBotsSample
//
//  Created by Radosław Czubak on 02/11/2026.
//  Copyright © 2026 KodaBots. All rights reserved.
//

import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?
    var rootCoordinator: RootCoordinator?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        window = UIWindow(windowScene: windowScene)
        let mainViewController = UINavigationController()
        rootCoordinator = RootCoordinator(navigationController: mainViewController)
        window?.rootViewController = mainViewController
        rootCoordinator?.start()
        window?.makeKeyAndVisible()
    }
}
