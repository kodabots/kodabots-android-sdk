//
//  Coordinator.swift
//  KodaBotsSample
//
//  Created by Felislynx.Silae on 12/10/2020.
//  Copyright © 2020 KodaBots. All rights reserved.
//

import Foundation
import UIKit

protocol Coordinator: AnyObject {
    var uuid: UUID {get}
    var childCoordinators: [UUID:Coordinator] { get set }
    
    // All coordinators will be initilised with a navigation controller
    init(navigationController:UINavigationController)
    
    func start()
    func onBackDetected(_ uuid:UUID)
}

protocol Identifyable {
    var uuid:UUID {get}
}
