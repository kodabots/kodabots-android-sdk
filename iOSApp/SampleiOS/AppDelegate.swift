//
//  AppDelegate.swift
//  KodaBotsSample
//
//  Created by Felislynx.Silae on 09/10/2020.
//  Copyright © 2020 KodaBots. All rights reserved.
//

import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    var rootCoordinator: RootCoordinator?
    
    func applicationDidFinishLaunching(_ application: UIApplication) {
        window = UIWindow(frame: UIScreen.main.bounds)
        let mainViewController = UINavigationController()
        rootCoordinator = RootCoordinator(navigationController: mainViewController)
        window?.rootViewController = mainViewController
        rootCoordinator?.start()
        UIPickerView.appearance().tintColor = UIColor.blue
        window?.makeKeyAndVisible()
    }
}

