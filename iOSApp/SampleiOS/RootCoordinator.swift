//
//  RootDelegate.swift
//  KodaBotsSample
//
//  Created by Felislynx.Silae on 12/10/2020.
//  Copyright © 2020 KodaBots. All rights reserved.
//

import Foundation
import UIKit

class RootCoordinator: NSObject, Coordinator {
    var uuid: UUID = UUID()
    var childCoordinators: [UUID:Coordinator] = [:]
    var currentUUID: UUID?

    weak var navigationController: UINavigationController?

    required init(navigationController: UINavigationController) {
        self.navigationController = navigationController
    }
    
    func start() {
        if let navigationController = self.navigationController {
            let coordinator = MainCoordinator(navigationController: navigationController)
            childCoordinators[coordinator.uuid] = coordinator
            currentUUID = coordinator.uuid
            coordinator.start()
        }
    }
    
    func onBackDetected(_ uuid: UUID) {}
}

