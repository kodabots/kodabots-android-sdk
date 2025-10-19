//
//  MainCoordinator.swift
//  KodaBotsSample
//
//  Created by Felislynx.Silae on 12/10/2020.
//  Copyright © 2020 KodaBots. All rights reserved.
//

import Foundation
import UIKit

class MainCoordinator: Coordinator {
    var uuid: UUID = UUID()
    var childCoordinators: [UUID:Coordinator] = [:]
    
    weak var navigationController:UINavigationController?
    
    required init(navigationController: UINavigationController) {
        self.navigationController = navigationController
    }
    
    func start() {
        let viewController: MainViewController = MainViewController(nibName: "MainViewController", bundle: nil)
        navigationController?.viewControllers = [viewController]
        navigationController?.setNavigationBarHidden(true, animated: false)
    }
    
    func onBackDetected(_ uuid: UUID) {
        
    }
}

